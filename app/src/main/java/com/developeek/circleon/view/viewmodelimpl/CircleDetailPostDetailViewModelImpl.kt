package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.viewmodel.CircleDetailPostDetailViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CircleDetailPostDetailViewModelImpl.CircleDetailPostDetailViewModelFactory::class)
class CircleDetailPostDetailViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circleId") private val circleId: Int,
        @Assisted("postId") private val postId: Int,
        private val repository: CircleRepository,
    ) : CircleDetailPostDetailViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailPostDetailViewModelFactory {
            fun create(
                @Assisted("circleId") circleId: Int,
                @Assisted("postId") postId: Int,
            ): CircleDetailPostDetailViewModelImpl
        }

        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override lateinit var comments: CommentModels
        private var commentLoadingJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        private lateinit var tmpState: UiState

        override lateinit var error: String

        init {
            loadComments()
        }

        private fun loadComments() {
            initCommentLoading()

            commentLoadingJob =
                viewModelScope.launch {
                    val result = repository.getPostComments(circleId, postId, currentPage, SIZE_BY_PAGE)

                    if (result is Success) {
                        comments = result.data
                        tmpState = UiState.Success
                    } else {
                        error = (result as Error).message()
                        if (result.isAuthenticationError()) {
                            tmpState = UiState.AuthenticationError
                        } else {
                            tmpState = UiState.ServiceError
                        }
                    }
                }
        }

        private fun initCommentLoading() {
            commentLoadingJob?.cancel()
            tmpState = UiState.Loading
            currentPage = DEFAULT_PAGE
        }

        override fun refresh() {
            loadComments()
            commentLoadingJob?.invokeOnCompletion {
                updateUiState()
            }
        }

        override fun updateUiState() {
            uiState.postValue(tmpState)
        }

        companion object {
            private const val SIZE_BY_PAGE = 50
            private const val DEFAULT_PAGE = 0
        }
    }
