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
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
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

        override val uploadCommentState: LiveData<UiState>
            get() = commentUploadState
        private val commentUploadState = MutableLiveData<UiState>()
        private var uploadCommentJob: Job? = null

        override val editCommentState: LiveData<UiState>
            get() = commentEditState
        private val commentEditState = MutableLiveData<UiState>()
        private var editCommentJob: Job? = null

        override val deleteCommentState: LiveData<UiState>
            get() = commentDeleteState
        private val commentDeleteState = MutableLiveData<UiState>()
        private var deleteCommentJob: Job? = null

        override val deletePostState: LiveData<UiState>
            get() = postDeleteState
        private val postDeleteState = MutableLiveData<UiState>()
        private var deletePostJob: Job? = null

        override lateinit var contents: List<Identifiable>
        override lateinit var comments: CommentModels
        private var fetchCommentsJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        override val scrollOver: LiveData<Boolean>
            get() = scrollOverCompleted
        private var scrollOverCompleted = MutableLiveData<Boolean>()
        private var scrollOverCommentJob: Job? = null
        override val scrollListener = RecyclerViewInfiniteScrollListener()
        override val currentScrollState: Parcelable?
            get() = scrollState
        private var scrollState: Parcelable? = null

        override lateinit var error: String

        init {
            fetchComments(currentPage, SIZE_BY_PAGE)
        }

        private fun fetchComments(
            page: Int,
            size: Int,
        ) {
            fetchCommentsJob?.let {
                if (!it.isCompleted) return
            }

            uiState.postValue(UiState.Loading)

            fetchCommentsJob =
                viewModelScope.launch {
                    val result = repository.getPostComments(circleId, post.id, page, size)

                    if (result is Success) {
                        comments = result.data
                        contents = listOf(post) + comments.get()
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

        override fun refresh() {
            fetchComments(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
        }

        override fun scrollOver() {
            scrollOverCommentJob?.let {
                if (!it.isCompleted) return
            }

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
                        val errorState =
                            if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
                        uiState.postValue(errorState)
                    }
                }
        }

        override fun saveScrollState(scrollState: Parcelable?) {
            this.scrollState = scrollState
        }

        override fun uploadComment(content: String) {
            if (!isCommentFormat(content)) return
            uploadCommentJob?.let {
                if (!it.isCompleted) return
            }

            commentUploadState.postValue(UiState.Loading)

            uploadCommentJob =
                viewModelScope.launch {
                    val result = repository.postCircleComment(circleId, post.id, content)

                    if (result is Success) {
                        refresh()
                        commentUploadState.postValue(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        val errorState =
                            if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
                        commentUploadState.postValue(errorState)
                    }
                }
        }

        override fun editComment(
            commentId: Int,
            content: String,
        ) {
            if (!isCommentFormat(content)) return
            editCommentJob?.let {
                if (!it.isCompleted) return
            }

            commentEditState.postValue(UiState.Loading)

            editCommentJob =
                viewModelScope.launch {
                    val result = repository.putCircleComment(circleId, post.id, commentId, content)

                    if (result is Success) {
                        refresh()
                        commentEditState.postValue(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        val errorState =
                            if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
                        commentEditState.postValue(errorState)
                    }
                }
        }

        override fun deleteComment(commentId: Int) {
            deleteCommentJob?.let {
                if (!it.isCompleted) return
            }

            commentDeleteState.postValue(UiState.Loading)

            deleteCommentJob =
                viewModelScope.launch {
                    val result = repository.deletePostComment(circleId, post.id, commentId)

                    if (result is Success) {
                        comments =
                            comments.remove(commentId).also {
                                if (comments.isLastPage()) it.setAsLast()
                            }
                        contents = listOf(post) + comments.get()
                        commentDeleteState.postValue(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        val errorState =
                            if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
                        commentDeleteState.postValue(errorState)
                    }
                }
        }

        private fun isCommentFormat(comment: String): Boolean {
            val validation = Validator.checkComment(comment)

            return if (validation is Invalid) {
                error = validation.message()
                commentUploadState.postValue(UiState.ServiceError)
                false
            } else {
                true
            }
        }

        override fun delete() {
            deletePostJob?.let {
                if (!it.isCompleted) return
            }

            postDeleteState.postValue(UiState.Loading)

            deletePostJob =
                viewModelScope.launch {
                    val result = repository.deleteCirclePost(circleId, post.id)

                    if (result is Success) {
                        postDeleteState.postValue(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        val errorState =
                            if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
                        postDeleteState.postValue(errorState)
                    }
                }
        }

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }
