package com.developeek.circleon.view.viewmodelimpl.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CategoryModel
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.circle.UploadCircleViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime

@HiltViewModel(assistedFactory = UploadCircleViewModelImpl.UploadCircleViewModelFactory::class)
class UploadCircleViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("origin") private val origin: CircleDetailModel?,
        @Assisted("isEdit") private val isEdit: Boolean,
        private val repository: CircleRepository,
    ) : UploadCircleViewModel, ViewModel() {
        @AssistedFactory
        interface UploadCircleViewModelFactory {
            fun create(
                @Assisted("origin") origin: CircleDetailModel?,
                @Assisted("isEdit") isEdit: Boolean,
            ): UploadCircleViewModelImpl
        }

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<UploadCircleScreen>(UploadCircleScreen.NormalView)
        override val screenFlow: StateFlow<UploadCircleScreen> = _screenFlow
        private val _uploadCircleScreenEvent = MutableSharedFlow<UploadCircleScreenEvent>()
        override val uploadCircleScreenEvent: SharedFlow<UploadCircleScreenEvent> = _uploadCircleScreenEvent

        override lateinit var circle: CircleDetailModel
        private var categories: Models<CategoryModel> = Models()

        private var uploadCircleJob: Job? = null
        private val dispatcher = Dispatchers.IO

        // 동아리 프로필, 소개글 이미지 등 이미지 처리 api 는 별도
        private var profileImage: File? = null
        private var introductionImage: File? = null

        /**
         * 이미지 파일이 null 인 경우 -> 1. 기존 이미지 유지 2. 기존 이미지 삭제
         *
         * 1. 이미지 null && !hasChanged == 기존 이미지 유지
         * 2. 이미지 null && hasChanged == 기존 이미지 삭제
         */
        private var hasProfileImageChanged = false
        private var hasIntroductionImageChanged = false

        init {
            origin?.let {
                circle = it
                categories = CategoryModel.selectAndGetWithoutALL(it.category)
            } ?: run {
                circle = CircleDetailModel.empty()
                categories = CategoryModel.selectAndGetWithoutALL(circle.category)
            }

            viewModelScope.launch {
                _uploadCircleScreenEvent.emit(UploadCircleScreenEvent.SelectCategory(categories))
            }
        }

        override fun upload() {
            uploadCircleJob?.let {
                if (!it.isCompleted) return
            }

            uploadCircleJob =
                viewModelScope.launch {
                    if (!checkCircleFormat(circle)) return@launch
                    _screenFlow.emit(UploadCircleScreen.LoadingView)

                    when (val result = postCircle(circle, profileImage, introductionImage)) {
                        is Success -> whenUploadCircleSuccess()
                        is Error -> whenUploadCircleFail(result)
                    }
                }
        }

        private suspend fun postCircle(
            circle: CircleDetailModel,
            profileImage: File?,
            introductionImage: File?,
        ) = withContext(dispatcher) {
            repository.postCircle(circle, profileImage, introductionImage)
        }

        private suspend fun whenUploadCircleSuccess() {
            val message = if (isEdit) MESSAGE_SUCCESS_EDIT_CIRCLE else MESSAGE_SUCCESS_UPLOAD_CIRCLE

            _event.emit(Event.ShowToast(message))
            _screenFlow.emit(UploadCircleScreen.SuccessView)
        }

        private suspend fun whenUploadCircleFail(result: Error<Unit>) {
            _event.emit(Event.ShowDialog(result.message()))
            _screenFlow.emit(UploadCircleScreen.NormalView)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun edit() {
            uploadCircleJob?.let {
                if (!it.isCompleted) return
            }

            uploadCircleJob =
                viewModelScope.launch {
                    if (!checkCircleFormat(circle)) return@launch
                    _screenFlow.emit(UploadCircleScreen.LoadingView)

                    val editCircle =
                        async {
                            editCircle(circle)
                        }

                    // 서버 설계 문제로 이미지 편집과 삭제 작업은 동기 통신 방식으로 진행
                    val editCircleImage =
                        async {
                            val editImage =
                                if (isAnyImageEdited()) {
                                    editCircleImage(circle, profileImage, introductionImage)
                                } else {
                                    Result.success(Unit)
                                }
                            val deleteImage =
                                if (isAnyImageRemoved()) {
                                    deleteCircleImage(circle)
                                } else {
                                    Result.success(Unit)
                                }

                            if (editImage is Success && deleteImage is Success) {
                                editImage
                            } else if (editImage is Error) {
                                editImage
                            } else {
                                deleteImage
                            }
                        }

                    awaitAll(editCircle, editCircleImage)
                        .find {
                            it is Error
                        }?.let {
                            whenUploadCircleFail(it as Error)
                        } ?: run {
                        whenUploadCircleSuccess()
                    }
                }
        }

        private suspend fun editCircle(circle: CircleDetailModel) =
            withContext(dispatcher) {
                repository.putCircle(circle)
            }

        private suspend fun editCircleImage(
            circle: CircleDetailModel,
            profileImage: File?,
            introductionImage: File?,
        ) = withContext(dispatcher) {
            repository.putCircleImage(circle.circleId, profileImage, introductionImage)
        }

        private suspend fun deleteCircleImage(circle: CircleDetailModel) =
            withContext(dispatcher) {
                repository.deleteCircleImage(circle.circleId, isProfileImageRemoved(), isIntroductionImageRemoved())
            }

        private suspend fun checkCircleFormat(circle: CircleDetailModel): Boolean {
            val result = Validator.checkCircle(circle)

            return if (result is Invalid) {
                _event.emit(Event.ShowDialog(result.message()))
                false
            } else {
                true
            }
        }

        override fun setCircleProfileImage(image: File?) {
            this.profileImage = image
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
            if (content == Const.EMPTY_TEXT) {
                this.circle = this.circle.fold(introduction = null)
            } else {
                this.circle = this.circle.fold(introduction = content)
            }
        }

        override fun setCategory(category: Category) {
            this.circle = this.circle.fold(category = category)
            categories = CategoryModel.selectAndGetWithoutALL(category)

            viewModelScope.launch {
                _uploadCircleScreenEvent.emit(UploadCircleScreenEvent.SelectCategory(categories))
            }
        }

        override fun toggleRecruitmentLock(isRecruiting: Boolean) {
            this.circle = this.circle.fold(recruiting = isRecruiting)

            viewModelScope.launch {
                _uploadCircleScreenEvent.emit(UploadCircleScreenEvent.ToggleRecruitmentLock(isRecruiting))
            }
        }

        override fun removeCircleProfileImage() {
            this.profileImage = null
            hasProfileImageChanged = true
        }

        override fun removeCircleIntroductionImage() {
            this.introductionImage = null
            hasIntroductionImageChanged = true
        }

        private fun isAnyImageEdited() = profileImage != null || introductionImage != null

        private fun isAnyImageRemoved() = isProfileImageRemoved() || isIntroductionImageRemoved()

        private fun isProfileImageRemoved() = profileImage == null && hasProfileImageChanged

        private fun isIntroductionImageRemoved() = introductionImage == null && hasIntroductionImageChanged

        companion object {
            private const val MESSAGE_SUCCESS_UPLOAD_CIRCLE = "동아리가 생성됐어요"
            private const val MESSAGE_SUCCESS_EDIT_CIRCLE = "동아리 정보가 수정됐어요"
        }
    }

sealed class UploadCircleScreen {
    data object SuccessView : UploadCircleScreen()

    data object LoadingView : UploadCircleScreen()

    data object NormalView : UploadCircleScreen()
}

sealed class UploadCircleScreenEvent {
    data class ToggleRecruitmentLock(val isRecruiting: Boolean) : UploadCircleScreenEvent()

    data class SelectCategory(val categories: Models<CategoryModel>) : UploadCircleScreenEvent()
}
