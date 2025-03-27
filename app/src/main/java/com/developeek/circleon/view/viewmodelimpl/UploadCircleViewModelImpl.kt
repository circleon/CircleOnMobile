package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CategoryModels
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.viewmodel.UploadCircleViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
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
        private var editJob: Job? = null
        override val categories: LiveData<CategoryModels>
            get() = circleCategories
        private var circleCategories =
            MutableLiveData(
                CategoryModels.selectAndRemoveAndGet(origin?.category ?: Category.ETC, Category.ALL),
            )

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

        override lateinit var error: String

        init {
            this.circle = origin ?: CircleDetailModel.empty()
        }

        override fun edit() {
            if (!isCircleFormat(circle)) return
            editJob?.let {
                if (!it.isCompleted) return
            }
            uiState.postValue(UiState.Loading)

            var tmpState: UiState = UiState.Success
            editJob =
                viewModelScope.launch {
                    val editCircleJob =
                        async {
                            return@async editCircle(circle)
                        }

                    // TODO: 서버 설계 문제로 이미지 편집과 삭제 작업은 동기 통신 방식으로 진행
                    val editCircleImageJob =
                        async {
                            val editImageState =
                                if (isAnyImageEdited()) {
                                    editCircleImage(circle)
                                } else {
                                    UiState.Success
                                }
                            val deleteImageState =
                                if (isAnyImageRemoved()) {
                                    deleteCircleImage(circle)
                                } else {
                                    UiState.Success
                                }

                            return@async mergeState(editImageState, deleteImageState)
                        }

                    val jobs: List<Deferred<UiState>> = listOf(editCircleJob, editCircleImageJob)
                    jobs.map { job ->
                        job.invokeOnCompletion {
                            if (job.isCancelled) {
                                this.cancel() // Error State 가 발생한 경우 부모 코루틴 캔슬
                            }
                        }
                    }
                    // 코루틴별 invokeOnCompletion 을 전부 등록해준 이후에 await() 을 해야 자식 코루틴 캔슬이 부모 코루틴에게 즉시 전파됨
                    jobs.awaitAll().map {
                        if (it !is UiState.Success) tmpState = it
                    }
                }.apply {
                    invokeOnCompletion {
                        uiState.postValue(tmpState)
                    }
                }
        }

        private suspend fun editCircle(circle: CircleDetailModel): UiState {
            val result = repository.putCircle(circle)

            if (result is Success) {
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        private suspend fun editCircleImage(circle: CircleDetailModel): UiState {
            val result = repository.putCircleImage(circle.id, thumbnail, introductionImage)

            if (result is Success) {
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        private suspend fun deleteCircleImage(circle: CircleDetailModel): UiState {
            val result = repository.deleteCircleImage(circle.id, isThumbnailRemoved(), isIntroductionImageRemoved())

            if (result is Success) {
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        private fun mergeState(
            state1: UiState,
            state2: UiState,
        ): UiState {
            if (state1 == UiState.ServiceError || state2 == UiState.ServiceError) {
                return UiState.ServiceError
            }
            if (state1 == UiState.AuthenticationError || state2 == UiState.AuthenticationError) {
                return UiState.AuthenticationError
            }
            return UiState.Success
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
            circleCategories.postValue(CategoryModels.selectAndRemoveAndGet(category, Category.ALL))
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
    }
