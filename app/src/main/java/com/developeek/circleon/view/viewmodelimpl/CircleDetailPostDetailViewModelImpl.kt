package com.developeek.circleon.view.viewmodelimpl

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.model.Identifiable
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
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
        @Assisted("post") private val post: PostModel,
        private val repository: CircleRepository,
    ) : CircleDetailPostDetailViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailPostDetailViewModelFactory {
            fun create(
                @Assisted("circleId") circleId: Int,
                @Assisted("post") post: PostModel,
            ): CircleDetailPostDetailViewModelImpl
        }

        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()
        private lateinit var tmpState: UiState

        override val registerCommentState: LiveData<Boolean>
            get() = commentRegisterState
        private val commentRegisterState = MutableLiveData<Boolean>()
        private var registerCommentJob: Job? = null

        override val deletePostState: LiveData<Boolean>
            get() = postDeleteState
        private val postDeleteState = MutableLiveData<Boolean>()
        private var deletePostJob: Job? = null

        override val deleteCommentState: LiveData<Boolean>
            get() = commentDeleteState
        private val commentDeleteState = MutableLiveData<Boolean>()
        private var deleteCommentJob: Job? = null

        override lateinit var contents: List<Identifiable>
        override lateinit var comments: CommentModels
        private var fetchCommentJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        override val scrollOver: LiveData<Boolean>
            get() = scrollOverCompleted
        private var scrollOverCompleted = MutableLiveData<Boolean>()
        private var scrollOverCommentJob: Job? = null
        override val scrollListener = RecyclerViewInfiniteScrollListener()
        override val currentScrollState: Parcelable?
            get() = scrollState
        private var scrollState: Parcelable? = null

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
            scrollOverCommentJob?.cancel()

            fetchCommentJob =
                viewModelScope.launch {
                    val result = repository.getPostComments(circleId, post.id, page, size)

                    if (result is Success) {
                        comments = result.data
                        contents = listOf(post) + comments.get()
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

        override fun scrollOver() {
            scrollOverCommentJob?.cancel()

            scrollOverCommentJob =
                viewModelScope.launch {
                    val result =
                        repository.getPostComments(
                            circleId, post.id, currentPage + 1, SIZE_BY_PAGE,
                        )

                    if (result is Success) {
                        comments =
                            comments.addAll(result.data).also {
                                if (result.data.isLastPage()) {
                                    it.setAsLast()
                                }
                            }
                        contents = listOf(post) + comments.get()
                        currentPage++
                        scrollOverCompleted.postValue(true)
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

        override fun saveScrollState(scrollState: Parcelable?) {
            this.scrollState = scrollState
        }

        override fun notifyEnterAnimFinishedAndUpdateUI() {
            enterAnimFinished = true
            if (::tmpState.isInitialized) uiState.postValue(tmpState)
        }

        override fun registerComment(comment: String) {
            registerCommentJob?.cancel()

            registerCommentJob =
                viewModelScope.launch {
                    val result = repository.postComment(circleId, post.id, comment)

                    if (result is Success) {
                        refresh()
                        commentRegisterState.postValueWhenAnimFinished(true)
                    } else {
                        error = (result as Error).message()
                        commentRegisterState.postValueWhenAnimFinished(false)
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

        override fun delete() {
            deletePostJob?.cancel()

            deletePostJob =
                viewModelScope.launch {
                    val result = repository.deletePost(circleId, post.id)

                    if (result is Success) {
                        postDeleteState.postValue(true)
                    } else {
                        error = (result as Error).message()
                        postDeleteState.postValue(false)
                    }
                }
        }

        override fun deleteComment(commentId: Int) {
            deleteCommentJob?.cancel()

            deleteCommentJob =
                viewModelScope.launch {
                    val result = repository.deleteComment(circleId, post.id, commentId)

                    if (result is Success) {
                        comments =
                            comments.remove(commentId).also {
                                if (comments.isLastPage()) it.setAsLast()
                            }
                        contents = listOf(post) + comments.get()
                        commentDeleteState.postValueWhenAnimFinished(true)
                    } else {
                        error = (result as Error).message()
                        commentDeleteState.postValueWhenAnimFinished(false)
                    }
                }
        }

        companion object {
            private const val SIZE_BY_PAGE = 10
            private const val DEFAULT_PAGE = 0
        }
    }
