package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.model.PostModels
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.home.CircleDetailPostViewModel
import com.developeek.circleon.view.viewmodelimpl.Page
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = CircleDetailPostViewModelImpl.CircleDetailPostViewModelFactory::class)
class CircleDetailPostViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circleDetail") private val circleDetail: CircleDetailModel,
        private val repository: CircleRepository,
    ) : CircleDetailPostViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailPostViewModelFactory {
            fun create(
                @Assisted("circleDetail") circleDetail: CircleDetailModel,
            ): CircleDetailPostViewModelImpl
        }

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<CircleDetailPostScreen>(CircleDetailPostScreen.LoadingView)
        override val screenFlow: StateFlow<CircleDetailPostScreen> = _screenFlow

        override val isLastPage: Boolean
            get() = _isLastPage
        private var _isLastPage = false

        private lateinit var posts: PostModels

        private var fetchPostsJob: Job? = null
        private var deletePostJob: Job? = null
        private var reportPostJob: Job? = null
        private var currentPage = DEFAULT_PAGE
        private val dispatcher = Dispatchers.IO
        private var scrollOverPostJob: Job? = null

        init {
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
                    scrollOverPostJob?.cancel()

                    when (val result = getCirclePosts(circleDetail.id, page, size)) {
                        is Success -> whenFetchPostsSuccess(result)
                        is Error -> whenFetchPostsFail(result)
                    }
                }
        }

        private suspend fun getCirclePosts(
            circleId: Int,
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            repository.getCirclePosts(circleId, page, size)
        }

        private suspend fun whenFetchPostsSuccess(result: Success<Page<PostModel>>) {
            result.data.let {
                posts = PostModels(it.content)
                _isLastPage = it.isLastPage
            }
            _screenFlow.emit(CircleDetailPostScreen.SuccessView(posts))
        }

        private suspend fun whenFetchPostsFail(result: Error<Page<PostModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(CircleDetailPostScreen.ErrorView)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun refresh() {
            viewModelScope.launch {
                _screenFlow.emit(CircleDetailPostScreen.LoadingView)
            }
            fetchPosts(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
        }

        override fun scrollOver() {
            scrollOverPostJob?.let {
                if (!it.isCompleted) return
            }

            scrollOverPostJob =
                viewModelScope.launch {
                    val result = getCirclePosts(circleDetail.id, currentPage + 1, SIZE_BY_PAGE)

                    if (!isActive) return@launch
                    when (result) {
                        is Success -> whenScrollOverSuccess(result)
                        is Error -> whenScrollOverFail(result)
                    }
                }
        }

        private suspend fun whenScrollOverSuccess(result: Success<Page<PostModel>>) {
            result.data.let {
                posts = posts.addAllAndGet(it.content)
                _isLastPage = it.isLastPage
            }
            currentPage++

            _screenFlow.emit(CircleDetailPostScreen.SuccessView(posts))
        }

        private suspend fun whenScrollOverFail(result: Error<Page<PostModel>>) {
            _event.emit(Event.ShowToast(result.message()))

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun deleteAndFetch(postId: Int) {
            deletePostJob?.let {
                if (!it.isCompleted) return
            }

//            deletePostJob =
//                viewModelScope.launch {
//                    val result = repository.deleteCirclePost(circleDetail.id, postId)
//
//                    if (result is Success) {
//                        _postState.postValue(UiState.Success)
//                        fetchPosts(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
//                    } else {
//                        error = (result as Error).message()
//                        if (result.isAuthenticationError()) {
//                            _postState.postValue(UiState.AuthenticationError)
//                        } else {
//                            _postState.postValue(UiState.ServiceError)
//                        }
//                    }
//                }
        }

        override fun requestReportPost(
            postId: Int,
            reportMessage: String,
        ) {
            reportPostJob?.let {
                if (!it.isCompleted) return
            }

//            reportPostJob =
//                viewModelScope.launch {
//                    val result = repository.postReportCirclePost(circleDetail.id, postId, content)
//
//                    if (result is Success) {
//                        _postState.postValue(UiState.Success)
//                    } else {
//                        error = (result as Error).message()
//                        if (result.isAuthenticationError()) {
//                            _postState.postValue(UiState.AuthenticationError)
//                        } else {
//                            _postState.postValue(UiState.ServiceError)
//                        }
//                    }
//                }
        }

        // 현재는 공지사항용 핀 고정 기능이고, 나중에 게시글 고정 기능 추가 시 사용
        override fun pinAndFetch(postId: Int) {}

        override fun removePinAndFetch(postId: Int) {}

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class CircleDetailPostScreen {
    data class SuccessView(val posts: PostModels) : CircleDetailPostScreen()

    data object LoadingView : CircleDetailPostScreen()

    data object ErrorView : CircleDetailPostScreen()
}
