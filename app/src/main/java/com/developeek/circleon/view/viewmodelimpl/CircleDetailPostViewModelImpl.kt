package com.developeek.circleon.view.viewmodelimpl

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.PostModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.viewmodel.CircleDetailPostViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CircleDetailPostViewModelImpl.CircleDetailPostViewModelFactory::class)
class CircleDetailPostViewModelImpl
    @AssistedInject
    constructor(
        @Assisted private val circleId: Int,
        private val repository: CircleRepository,
    ) : CircleDetailPostViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailPostViewModelFactory {
            fun create(circleId: Int): CircleDetailPostViewModelImpl
        }

        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override val posts: PostModels
            get() = postModels
        private var postModels = PostModels.empty()
        private var fetchPostsJob: Job? = null
        private var deletePostJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        override val scrollOver: LiveData<Boolean>
            get() = scrollOverCompleted
        private var scrollOverCompleted = MutableLiveData<Boolean>()
        private var scrollOverPostJob: Job? = null
        override val scrollListener = RecyclerViewInfiniteScrollListener()
        override val currentScrollState: Parcelable?
            get() = scrollState
        private var scrollState: Parcelable? = null
        override val currentTopOrNot: Boolean
            get() = isTop
        private var isTop = true

        override lateinit var error: String

        init {
            uiState.postValue(UiState.Loading)
            fetchPosts(currentPage, SIZE_BY_PAGE)
        }

        private fun fetchPosts(
            page: Int,
            size: Int,
        ) {
            fetchPostsJob?.let {
                if (!it.isCompleted) return
            }

            fetchPostsJob =
                viewModelScope.launch {
                    val result = repository.getCirclePosts(circleId, page, size)

                    if (result is Success) {
                        postModels = result.data
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
            uiState.postValue(UiState.Loading)
            fetchPosts(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
        }

        override fun scrollOver() {
            scrollOverPostJob?.let {
                if (!it.isCompleted) return
            }

            scrollOverPostJob =
                viewModelScope.launch {
                    val result = repository.getCirclePosts(circleId, currentPage + 1, SIZE_BY_PAGE)

                    if (result is Success) {
                        postModels =
                            postModels.addAll(result.data).also {
                                if (result.data.isLastPage()) {
                                    it.setAsLast()
                                }
                            }
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

        override fun setTopOrNot(isTop: Boolean) {
            this.isTop = isTop
        }

        override fun deleteAndFetch(postId: Int) {
            deletePostJob?.let {
                if (!it.isCompleted) return
            }

            deletePostJob =
                viewModelScope.launch {
                    val result = repository.deleteCirclePost(circleId, postId)

                    if (result is Success) {
                        fetchPosts(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
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

        // 현재는 공지사항용 핀 고정 기능이고, 나중에 게시글 고정 기능 추가 시 사용
        override fun pinAndFetch(postId: Int) {}

        override fun removePinAndFetch(postId: Int) {}

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }
