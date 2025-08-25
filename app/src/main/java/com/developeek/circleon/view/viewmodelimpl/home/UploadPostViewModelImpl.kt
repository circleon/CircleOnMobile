package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.model.PostEditResultModel
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
        private val repository: CircleRepository,
    ) : UploadPostViewModel, ViewModel() {
        @AssistedFactory
        interface UploadPostViewModelFactory {
            fun create(
                @Assisted("circleId") circleId: Int,
                @Assisted("postType") postType: PostType,
            ): UploadPostViewModelImpl
        }

        private var _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private var _screenFlow = MutableStateFlow<UploadPostScreen>(UploadPostScreen.Normal)
        override val screenFlow: StateFlow<UploadPostScreen> = _screenFlow
        override lateinit var postEditResult: PostEditResultModel

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
                    _screenFlow.emit(UploadPostScreen.Loading)

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
            _screenFlow.emit(UploadPostScreen.Success)
        }

        private suspend fun whenUploadPostFail(result: Error<Unit>) {
            _event.emit(Event.ShowDialog(result.message()))
            _screenFlow.emit(UploadPostScreen.Normal)

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
                    _screenFlow.emit(UploadPostScreen.Loading)

                    when (val result = putCirclePost(circleId, postId, postType, content)) {
                        is Success -> {
                            postEditResult = result.data
                            whenUploadPostSuccess()
                        }
                        is Error -> whenEditPostFail(result)
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

        private suspend fun whenEditPostFail(result: Error<PostEditResultModel>) {
            _event.emit(Event.ShowDialog(result.message()))
            _screenFlow.emit(UploadPostScreen.Normal)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun setPostImage(image: File?) {
            this.image = image
        }

        override fun removePostImage() {
            this.image = null
        }
    }

sealed class UploadPostScreen {
    data object Success : UploadPostScreen()

    data object Loading : UploadPostScreen()

    data object Normal : UploadPostScreen()
}
