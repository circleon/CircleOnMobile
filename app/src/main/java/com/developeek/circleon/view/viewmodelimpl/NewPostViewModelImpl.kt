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
import com.developeek.circleon.view.viewmodel.NewPostViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel(assistedFactory = NewPostViewModelImpl.NewPostViewModelFactory::class)
class NewPostViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circleId") private val circleId: Int,
        @Assisted("postType") private val postType: PostType,
        private val repository: CircleRepository,
    ) : NewPostViewModel, ViewModel() {
        @AssistedFactory
        interface NewPostViewModelFactory {
            fun create(
                @Assisted("circleId") circleId: Int,
                @Assisted("postType") postType: PostType,
            ): NewPostViewModelImpl
        }

        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        private var image: File? = null
        private var uploadPostJob: Job? = null

        override lateinit var error: String

        override fun upload(content: String) {
            uploadPostJob?.cancel()
            uiState.postValue(UiState.Loading)

            uploadPostJob =
                viewModelScope.launch {
                    val result = repository.postCirclePost(circleId, postType, content, image)

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

        override fun setPostImage(image: File?) {
            this.image = image
        }

        override fun removePostImage() {
            this.image = null
        }
    }
