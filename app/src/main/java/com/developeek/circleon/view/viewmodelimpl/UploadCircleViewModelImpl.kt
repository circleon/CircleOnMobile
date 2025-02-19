package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.viewmodel.UploadCircleViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime

@HiltViewModel(assistedFactory = UploadCircleViewModelImpl.UploadCircleViewModelFactory::class)
class UploadCircleViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("origin") private val origin: CircleDetailModel?,
        private val repository: CircleRepository,
    ) : UploadCircleViewModel, ViewModel() {
        @AssistedFactory
        interface UploadCircleViewModelFactory {
            fun create(
                @Assisted("origin") origin: CircleDetailModel?,
            ): UploadCircleViewModelImpl
        }

        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override lateinit var circle: CircleDetailModel
        private var uploadJob: Job? = null

        // 썸네일, 소개글 이미지 등 이미지 처리 api 는 별도
        private var thumbnail: File? = null
        private var introductionImage: File? = null

        /**
         * 이미지 파일이 null 인 경우 -> 1. 기존 이미지 유지 2. 기존 이미지 삭제
         *
         * 1. 이미지 null && !hasChanged == 기존 이미지 유지
         * 2. 이미지 null && hasChanged == 기존 이미지 삭제
         */
        private var hasThumbnailChanged = false
        private var hasIntroductionImageChanged = false

        private var loadingStartTime = 0L
        override lateinit var error: String

        init {
            this.circle = origin ?: CircleDetailModel.empty()
        }

        override fun edit() {
            if (!isCircleFormat(this.circle)) return
            uploadJob?.let {
                if (!it.isCompleted) return
            }
            uiState.postValue(UiState.Loading)
            saveLoadingStartTime()

            uploadJob =
                viewModelScope.launch {
                    launch {
                        editCircle(this, this@UploadCircleViewModelImpl.circle)
                    }
                    launch {
                        if (isAnyImageEdited()) {
                            editCircleImage(this, this@UploadCircleViewModelImpl.circle)
                        }
                    }
                    launch {
                        if (isAnyImageRemoved()) {
                            deleteCircleImage(this, this@UploadCircleViewModelImpl.circle)
                        }
                    }
                }.apply {
                    invokeOnCompletion {
                        if (!isCancelled) {
                            uiState.postValue(UiState.Success)
                        }
                    }
                }
        }

        private suspend fun editCircle(
            scope: CoroutineScope,
            circle: CircleDetailModel,
        ) {
            val result = repository.putCircle(circle)
            delay(remainedLoadingTime())

            if (result is Error) {
                error = result.message()
                if (result.isAuthenticationError()) {
                    uiState.postValue(UiState.AuthenticationError)
                } else {
                    uiState.postValue(UiState.ServiceError)
                }
                scope.cancel()
            }
        }

        private suspend fun editCircleImage(
            scope: CoroutineScope,
            circle: CircleDetailModel,
        ) {
            val result = repository.putCircleImage(circle.id, thumbnail, introductionImage)

            if (result is Error) {
                error = result.message()
                if (result.isAuthenticationError()) {
                    uiState.postValue(UiState.AuthenticationError)
                } else {
                    uiState.postValue(UiState.ServiceError)
                }
                scope.cancel()
            }
        }

        private suspend fun deleteCircleImage(
            scope: CoroutineScope,
            circle: CircleDetailModel,
        ) {
            val result = repository.deleteCircleImage(circle.id, isThumbnailRemoved(), isIntroductionImageRemoved())

            if (result is Error) {
                error = result.message()
                if (result.isAuthenticationError()) {
                    uiState.postValue(UiState.AuthenticationError)
                } else {
                    uiState.postValue(UiState.ServiceError)
                }
                scope.cancel()
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

        override fun setCircleThumbnail(image: File?) {
            this.thumbnail = image
        }

        override fun setCircleIntroductionImage(image: File?) {
            this.introductionImage = image
        }

        override fun setCircleName(name: String) {
            this.circle = this.circle.fold(name = name)
        }

        override fun setRecruitmentStartDate(date: LocalDateTime) {
            this.circle = this.circle.fold(recruitmentStartDate = date)
        }

        override fun setRecruitmentEndDate(date: LocalDateTime) {
            this.circle = this.circle.fold(recruitmentEndDate = date)
        }

        override fun setSingleLineIntroduction(content: String) {
            this.circle = this.circle.fold(singleLineIntroduction = content)
        }

        override fun setIntroduction(content: String) {
            this.circle = this.circle.fold(introduction = content)
        }

        override fun setCategory(category: Category) {
            this.circle = this.circle.fold(category = category)
        }

        override fun removeCircleThumbnail() {
            this.thumbnail = null
            hasThumbnailChanged = true
        }

        override fun removeCircleIntroductionImage() {
            this.introductionImage = null
            hasIntroductionImageChanged = true
        }

        private fun isAnyImageEdited() = thumbnail != null || introductionImage != null

        private fun isAnyImageRemoved() = isThumbnailRemoved() || isIntroductionImageRemoved()

        private fun isThumbnailRemoved() = thumbnail == null && hasThumbnailChanged

        private fun isIntroductionImageRemoved() = introductionImage == null && hasIntroductionImageChanged

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
