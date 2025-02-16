package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.viewmodel.UploadCircleViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class UploadCircleViewModelImpl
    @Inject
    constructor(
        private val repository: CircleRepository,
    ) : UploadCircleViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override lateinit var origin: CircleDetailModel
        private var temp: CircleDetailModel = CircleDetailModel.empty()
        private var uploadCircleJob: Job? = null

        // 썸네일, 소개글 이미지 등 이미지 처리 api 는 별도
        private var thumbnail: File? = null
        private var introductionImage: File? = null

        private var loadingStartTime = 0L
        override lateinit var error: String

        override fun edit() {
            val circle = if (origin == temp) origin else temp
            if (!isCircleFormat(circle)) return

            uploadCircleJob?.cancel()
            uiState.postValue(UiState.Loading)
            saveLoadingStartTime()

            uploadCircleJob =
                viewModelScope.launch {
                    val result = repository.putCircle(circle)
                    delay(remainedLoadingTime())

                    if (result is Success) {
                        uiState.postValue(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        if (result.isAuthenticationError()) {
                            uiState.postValue(UiState.AuthenticationError)
                        } else {
                            uiState.postValue(UiState.ServiceError)
                        }
                    }
                }
        }

        private fun isCircleFormat(circle: CircleDetailModel): Boolean {
            val validation = Validator.checkCircle(circle)

            return if (validation is Invalid) {
                error = validation.message()
                uiState.postValue(UiState.ServiceError)
                false
            } else {
                true
            }
        }

        override fun origin(circle: CircleDetailModel) {
            this.origin = circle
            this.temp = origin
        }

        override fun setCircleThumbnail(image: File?) {
            this.thumbnail = image
        }

        override fun setCircleIntroductionImage(image: File?) {
            this.introductionImage = image
        }

        override fun setCircleName(name: String) {
            this.temp = temp.fold(name = name)
        }

        override fun setRecruitmentStartDate(date: LocalDateTime) {
            this.temp = temp.fold(recruitmentStartDate = date)
        }

        override fun setRecruitmentEndDate(date: LocalDateTime) {
            this.temp = temp.fold(recruitmentEndDate = date)
        }

        override fun setSingleLineIntroduction(content: String) {
            this.temp = temp.fold(singleLineIntroduction = content)
        }

        override fun setIntroduction(content: String) {
            this.temp = temp.fold(introduction = content)
        }

        override fun setCategory(category: Category) {
            this.temp = temp.fold(category = category)
        }

        override fun removeCircleThumbnail() {
            this.thumbnail = null
        }

        override fun removeCircleIntroductionImage() {
            this.introductionImage = null
        }

        private fun saveLoadingStartTime() {
            this.loadingStartTime = System.currentTimeMillis()
        }

        private fun remainedLoadingTime(): Long {
            val remainTime = MAX_DEFAULT_ANIM_TIME_MILLIS - (System.currentTimeMillis() - loadingStartTime)

            return if (remainTime > 0) remainTime else 0
        }

        companion object {
            private const val MAX_DEFAULT_ANIM_TIME_MILLIS = 200L
        }
    }
