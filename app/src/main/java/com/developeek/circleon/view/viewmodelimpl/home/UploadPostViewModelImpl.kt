package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.home.UploadPostViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@HiltViewModel(assistedFactory = UploadPostViewModelImpl.UploadPostViewModelFactory::class)
class UploadPostViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circleId") private val circleId: Int,
        @Assisted("postType") private val postType: PostType,
        @Assisted("isEdit") private val isEdit: Boolean,
        private val repository: CircleRepository,
    ) : UploadPostViewModel, ViewModel() {
        @AssistedFactory
        interface UploadPostViewModelFactory {
            fun create(
                @Assisted("circleId") circleId: Int,
                @Assisted("postType") postType: PostType,
                @Assisted("isEdit") isEdit: Boolean,
            ): UploadPostViewModelImpl
        }

        private var _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private var _screenFlow = MutableStateFlow<UploadPostScreen>(UploadPostScreen.NormalView)
        override val screenFlow: StateFlow<UploadPostScreen> = _screenFlow

        private var image: File? = null
        private var uploadPostJob: Job? = null
        private val dispatcher = Dispatchers.IO

        override fun upload(content: String) {
            uploadPostJob?.let {
                if (!it.isCompleted) return
            }

            uploadPostJob =
                viewModelScope.launch {
                    if (!checkPostFormat(content)) return@launch
                    _screenFlow.emit(UploadPostScreen.LoadingView)

                    when (val result = postCirclePost(circleId, postType, content, image)) {
                        is Success -> whenUploadPostSuccess()
                        is Error -> whenUploadPostFail(result)
                    }
                }
        }

        private suspend fun postCirclePost(
            circleId: Int,
            postType: PostType,
            content: String,
            image: File?,
        ) = withContext(dispatcher) {
            repository.postCirclePost(circleId, postType, content, image)
        }

        private suspend fun whenUploadPostSuccess() {
            val message =
                when (postType) {
                    PostType.POST -> if (isEdit) MESSAGE_SUCCESS_EDIT_POST else MESSAGE_SUCCESS_UPLOAD_POST
                    PostType.NOTICE -> if (isEdit) MESSAGE_SUCCESS_EDIT_NOTICE else MESSAGE_SUCCESS_UPLOAD_NOTICE
                }

            _event.emit(Event.ShowToast(message))
            _screenFlow.emit(UploadPostScreen.SuccessView)
        }

        private suspend fun whenUploadPostFail(result: Error<Unit>) {
            _event.emit(Event.ShowDialog(result.message()))
            _screenFlow.emit(UploadPostScreen.NormalView)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun edit(
            postId: Int,
            content: String,
        ) {
            uploadPostJob?.let {
                if (!it.isCompleted) return
            }

            uploadPostJob =
                viewModelScope.launch {
                    if (!checkPostFormat(content)) return@launch
                    _screenFlow.emit(UploadPostScreen.LoadingView)

                    when (val result = putCirclePost(circleId, postId, postType, content)) {
                        is Success -> whenUploadPostSuccess()
                        is Error -> whenUploadPostFail(result)
                    }
                }
        }

        private suspend fun putCirclePost(
            circleId: Int,
            postId: Int,
            postType: PostType,
            content: String,
        ) = withContext(dispatcher) {
            repository.putCirclePost(circleId, postId, postType, content)
        }

        private suspend fun checkPostFormat(content: String): Boolean {
            val result = Validator.checkPost(content)

            return if (result is Invalid) {
                _event.emit(Event.ShowDialog(result.message()))
                false
            } else {
                true
            }
        }

        override fun setPostImage(image: File?) {
            this.image = image
        }

        override fun removePostImage() {
            this.image = null
        }

        companion object {
            private const val MESSAGE_SUCCESS_UPLOAD_POST = "게시글이 작성됐어요"
            private const val MESSAGE_SUCCESS_UPLOAD_NOTICE = "공지사항이 작성됐어요"
            private const val MESSAGE_SUCCESS_EDIT_POST = "게시글이 수정됐어요"
            private const val MESSAGE_SUCCESS_EDIT_NOTICE = "공지사항이 수정됐어요"
        }
    }

sealed class UploadPostScreen {
    data object SuccessView : UploadPostScreen()

    data object LoadingView : UploadPostScreen()

    data object NormalView : UploadPostScreen()
}
