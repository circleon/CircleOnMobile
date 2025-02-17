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

@HiltViewModel(assistedFactory = CircleDetailNoticeViewModelImpl.CircleDetailNoticeViewModelFactory::class)
class CircleDetailNoticeViewModelImpl
    @AssistedInject
    constructor(
        @Assisted private val circleId: Int,
        private val repository: CircleRepository,
    ) : CircleDetailPostViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailNoticeViewModelFactory {
            fun create(circleId: Int): CircleDetailNoticeViewModelImpl
        }

        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override lateinit var posts: PostModels
        private var fetchNoticeJob: Job? = null
        private var pinNoticeJob: Job? = null
        private var deleteNoticeJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        override val scrollOver: LiveData<Boolean>
            get() = scrollOverCompleted
        private var scrollOverCompleted = MutableLiveData<Boolean>()
        private var scrollOverNoticeJob: Job? = null
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
            fetchNotices(currentPage, SIZE_BY_PAGE)
        }

        private fun fetchNotices(
            page: Int,
            size: Int,
        ) {
            fetchNoticeJob?.let {
                if (!it.isCompleted) return
            }

            fetchNoticeJob =
                viewModelScope.launch {
                    val result = repository.getCircleNotices(circleId, page, size)

                    if (result is Success) {
                        posts = result.data
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
            // currentPage: 0 == page 1
            // currentPage: 1 == page 2 ...
            fetchNotices(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
        }

        override fun scrollOver() {
            scrollOverNoticeJob?.let {
                if (!it.isCompleted) return
            }

            scrollOverNoticeJob =
                viewModelScope.launch {
                    val result = repository.getCircleNotices(circleId, currentPage + 1, SIZE_BY_PAGE)

                    if (result is Success) {
                        posts =
                            posts.addAll(result.data).also {
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

        override fun pinAndRefresh(postId: Int) {
            togglePinAndRefresh(postId, true)
        }

        override fun removePinAndRefresh(postId: Int) {
            togglePinAndRefresh(postId, false)
        }

        private fun togglePinAndRefresh(
            postId: Int,
            isPinned: Boolean,
        ) {
            pinNoticeJob?.let {
                if (!it.isCompleted) return
            }

            pinNoticeJob =
                viewModelScope.launch {
                    val result = repository.putPostPin(circleId, postId, isPinned)

                    if (result is Success) {
                        refresh()
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

        override fun deleteAndRefresh(postId: Int) {
            deleteNoticeJob?.let {
                if (!it.isCompleted) return
            }

            deleteNoticeJob =
                viewModelScope.launch {
                    val result = repository.deleteCirclePost(circleId, postId)

                    if (result is Success) {
                        refresh()
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

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }
