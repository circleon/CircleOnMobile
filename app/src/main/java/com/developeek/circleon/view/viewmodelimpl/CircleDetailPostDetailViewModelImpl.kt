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

        private var enterAnimFinished = false
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
                        uiState.postValueWhenAnimFinished(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        if (result.isAuthenticationError()) {
                            uiState.postValueWhenAnimFinished(UiState.AuthenticationError)
                        } else {
                            uiState.postValueWhenAnimFinished(UiState.ServiceError)
                        }
                    }
                }
        }

        private fun initCommentLoading() {
            uiState.postValueWhenAnimFinished(UiState.Loading)
            commentLoadingJob?.cancel()
            currentPage = DEFAULT_PAGE
        }

        override fun refresh() {
            loadComments()
        }

        override fun notifyEnterAnimFinishedAndUpdateUI() {
            enterAnimFinished = true
            if (::tmpState.isInitialized) uiState.postValue(tmpState)
        }

        /**
         * MutableLiveData.postValue()
         *
         * enterAnim 이 종료되지 않은 경우 tmpState 에 저장
         * enterAnim 이 종료된 경우 postValue
         */
        private fun MutableLiveData<UiState>.postValueWhenAnimFinished(data: UiState) {
            if (enterAnimFinished) {
                this.postValue(data)
            } else {
                tmpState = data
            }
        }

        companion object {
            private const val SIZE_BY_PAGE = 50
            private const val DEFAULT_PAGE = 0
        }
    }
