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
import kotlinx.coroutines.delay
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

        override val registerCommentState: LiveData<UiState>
            get() = commentRegisterState
        private val commentRegisterState = MutableLiveData<UiState>()
        private var registerCommentJob: Job? = null

        override val deletePostState: LiveData<UiState>
            get() = postDeleteState
        private val postDeleteState = MutableLiveData<UiState>()
        private var deletePostJob: Job? = null

        override val deleteCommentState: LiveData<UiState>
            get() = commentDeleteState
        private val commentDeleteState = MutableLiveData<UiState>()
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
        private var loadingStartTime = 0L

        override lateinit var error: String

        init {
            uiState.postValue(UiState.Loading)
            saveLoadingStartTime()
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
                    delay(remainedLoadingTime())

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
                        val errorState =
                            if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
                        uiState.postValueWhenAnimFinished(errorState)
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
            commentRegisterState.postValueWhenAnimFinished(UiState.Loading)
            saveLoadingStartTime()

            registerCommentJob =
                viewModelScope.launch {
                    val result = repository.postCircleComment(circleId, post.id, comment)
                    delay(remainedLoadingTime())

                    if (result is Success) {
                        delay(200)
                        refresh()
                        commentRegisterState.postValueWhenAnimFinished(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        val errorState =
                            if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
                        commentRegisterState.postValueWhenAnimFinished(errorState)
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
            postDeleteState.postValue(UiState.Loading)
            saveLoadingStartTime()

            deletePostJob =
                viewModelScope.launch {
                    val result = repository.deleteCirclePost(circleId, post.id)
                    delay(remainedLoadingTime())

                    if (result is Success) {
                        postDeleteState.postValueWhenAnimFinished(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        val errorState =
                            if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
                        postDeleteState.postValueWhenAnimFinished(errorState)
                    }
                }
        }

        override fun deleteComment(commentId: Int) {
            deleteCommentJob?.cancel()
            commentDeleteState.postValueWhenAnimFinished(UiState.Loading)
            saveLoadingStartTime()

            deleteCommentJob =
                viewModelScope.launch {
                    val result = repository.deletePostComment(circleId, post.id, commentId)
                    delay(remainedLoadingTime())

                    if (result is Success) {
                        comments =
                            comments.remove(commentId).also {
                                if (comments.isLastPage()) it.setAsLast()
                            }
                        contents = listOf(post) + comments.get()
                        commentDeleteState.postValueWhenAnimFinished(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        val errorState =
                            if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
                        commentDeleteState.postValueWhenAnimFinished(errorState)
                    }
                }
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
            private const val SIZE_BY_PAGE = 10
            private const val DEFAULT_PAGE = 0
            private const val MAX_DEFAULT_ANIM_TIME_MILLIS = 200L
        }
    }
