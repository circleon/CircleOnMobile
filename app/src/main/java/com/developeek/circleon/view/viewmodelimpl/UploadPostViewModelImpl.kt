package com.developeek.circleon.view.viewmodelimpl

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
import com.developeek.circleon.view.viewmodel.UploadPostViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        private var loadingStartTime = 0L

        override lateinit var error: String

        override fun upload(content: String) {
            if (!isContentFormat(content)) return

            uploadPostJob?.cancel()
            uiState.postValue(UiState.Loading)
            saveLoadingStartTime()

            uploadPostJob =
                viewModelScope.launch {
                    val result = repository.postCirclePost(circleId, postType, content, image)
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

        override fun edit(
            postId: Int,
            content: String,
        ) {
            if (!isContentFormat(content)) return

            uploadPostJob?.cancel()
            uiState.postValue(UiState.Loading)
            saveLoadingStartTime()

            uploadPostJob =
                viewModelScope.launch {
                    val result = repository.putCirclePost(circleId, postId, postType, content)
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

        private fun isContentFormat(content: String): Boolean {
            val validation = Validator.checkContent(content)

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

        private fun saveLoadingStartTime() {
            this.loadingStartTime = System.currentTimeMillis()
        }

        /**
         * remainedLoadingTime()
         *
         * 코루틴 수행 시 LoadingState 에 머무르는 최소 시간을 계산하여 보장
         */
        private fun remainedLoadingTime(): Long {
            val remainTime = MAX_DEFAULT_ANIM_TIME_MILLIS - (System.currentTimeMillis() - loadingStartTime)

            return if (remainTime > 0) remainTime else 0
        }

        companion object {
            private const val MAX_DEFAULT_ANIM_TIME_MILLIS = 200L
        }
    }
