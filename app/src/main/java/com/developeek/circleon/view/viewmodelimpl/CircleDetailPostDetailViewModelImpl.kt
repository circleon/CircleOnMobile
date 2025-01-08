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
        private lateinit var tmpState: UiState

        override val registerCommentState: LiveData<Boolean> // 댓글 등록 성공 확인용
            get() = commentState
        private val commentState = MutableLiveData<Boolean>()

        override lateinit var comments: CommentModels
        private var fetchCommentJob: Job? = null
        private var registerCommentJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        private var enterAnimFinished = false

        override lateinit var error: String

        init {
            uiState.postValueWhenAnimFinished(UiState.Loading)
            fetchComments(currentPage, SIZE_BY_PAGE)
        }

        private fun fetchComments(
            page: Int,
            size: Int,
        ) {
            fetchCommentJob?.cancel()

            fetchCommentJob =
                viewModelScope.launch {
                    val result = repository.getPostComments(circleId, postId, page, size)

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

        override fun refresh() {
            fetchComments(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
        }

        override fun notifyEnterAnimFinishedAndUpdateUI() {
            enterAnimFinished = true
            if (::tmpState.isInitialized) uiState.postValue(tmpState)
        }

        override fun registerComment(comment: String) {
            registerCommentJob?.cancel()

            registerCommentJob =
                viewModelScope.launch {
                    val result = repository.postComment(circleId, postId, comment)

                    if (result is Success) {
                        comments = comments.add(result.data)
                        commentState.postValueWhenAnimFinished(true)
                    } else {
                        error = (result as Error).message()
                        commentState.postValueWhenAnimFinished(false)
                        if (result.isAuthenticationError()) {
                            uiState.postValueWhenAnimFinished(UiState.AuthenticationError)
                        } else {
                            uiState.postValueWhenAnimFinished(UiState.ServiceError)
                        }
                    }
                }
        }

        /**
         * MutableLiveData.postValue()
         *
         * - UiState
         * enterAnim 이 종료되지 않은 경우 tmpState 에 저장
         * enterAnim 이 종료된 경우 postValue
         *
         * - Boolean(registerComment)
         * enterAnim 이 종료된 경우에만 postValue
         */
        private fun MutableLiveData<UiState>.postValueWhenAnimFinished(data: UiState) {
            if (enterAnimFinished) {
                this.postValue(data)
            } else {
                tmpState = data
            }
        }

        private fun MutableLiveData<Boolean>.postValueWhenAnimFinished(data: Boolean) {
            if (enterAnimFinished) {
                this.postValue(data)
            }
        }

        companion object {
            private const val SIZE_BY_PAGE = 50
            private const val DEFAULT_PAGE = 0
        }
    }
