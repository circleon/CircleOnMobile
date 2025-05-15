package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.viewmodel.home.UploadPostViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        private var image: File? = null
        private var uploadPostJob: Job? = null
        private val dispatcher = Dispatchers.IO

        override lateinit var error: String

        override fun upload(content: String) {
            if (!isPostFormat(content)) return
            uploadPostJob?.let {
                if (!it.isCompleted) return
            }
            uiState.postValue(UiState.Loading)

            uploadPostJob =
                viewModelScope.launch {
                    val result = postCirclePost(circleId, postType, content, image)

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

        private suspend fun postCirclePost(
            circleId: Int,
            postType: PostType,
            content: String,
            image: File?,
        ) = withContext(dispatcher) {
            repository.postCirclePost(circleId, postType, content, image)
        }

        override fun edit(
            postId: Int,
            content: String,
        ) {
            if (!isPostFormat(content)) return
            uploadPostJob?.let {
                if (!it.isCompleted) return
            }
            uiState.postValue(UiState.Loading)

            uploadPostJob =
                viewModelScope.launch {
                    val result = putCirclePost(circleId, postId, postType, content)

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

        private suspend fun putCirclePost(
            circleId: Int,
            postId: Int,
            postType: PostType,
            content: String,
        ) = withContext(dispatcher) {
            repository.putCirclePost(circleId, postId, postType, content)
        }

        private fun isPostFormat(content: String): Boolean {
            val validation = Validator.checkPost(content)

            return if (validation is Invalid) {
                error = validation.message()
                uiState.postValue(UiState.ServiceError)
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
    }
