package com.developeek.circleon.view.viewmodelimpl.home

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.repository.Page
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.home.CircleDetailPostViewModel
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
        private var currentPage = DEFAULT_PAGE

        private lateinit var posts: Models<PostModel>
        override val currentScrollState: Parcelable?
            get() = _currentScrollState
        private var _currentScrollState: Parcelable? = null

        private var fetchPostsJob: Job? = null
        private var scrollOverPostJob: Job? = null
        private var userRequestJob: Job? = null
        private val dispatcher = Dispatchers.IO

        init {
            fetchPosts(currentPage, SIZE_BY_PAGE)
        }

        override fun showLoadingAndRefresh() {
            viewModelScope.launch {
                _screenFlow.emit(CircleDetailPostScreen.LoadingView)
            }
            refresh()
        }

        override fun refresh() {
            fetchPosts(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
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

                    when (val result = getCirclePosts(circleDetail.circleId, page, size)) {
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
                _isLastPage = it.isLastPage
                posts = Models(it.content)
                _screenFlow.emit(CircleDetailPostScreen.SuccessView(posts))
            }
        }

        private suspend fun whenFetchPostsFail(result: Error<Page<PostModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(CircleDetailPostScreen.ErrorView)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun scrollOver() {
            scrollOverPostJob?.let {
                if (!it.isCompleted) return
            }

            scrollOverPostJob =
                viewModelScope.launch {
                    val result = getCirclePosts(circleDetail.circleId, currentPage + 1, SIZE_BY_PAGE)

                    if (!isActive) return@launch
                    when (result) {
                        is Success -> whenScrollOverSuccess(result)
                        is Error -> whenScrollOverFail(result)
                    }
                }
        }

        private suspend fun whenScrollOverSuccess(result: Success<Page<PostModel>>) {
            result.data.let {
                _isLastPage = it.isLastPage
                posts = posts.addAllAndGet(it.content)
                _screenFlow.emit(CircleDetailPostScreen.SuccessView(posts))
            }
            currentPage++
        }

        private suspend fun whenScrollOverFail(result: Error<Page<PostModel>>) {
            _event.emit(Event.ShowToast(result.message()))

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun deleteAndFetch(postId: Int) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)

                    when (val result = deleteCirclePost(circleDetail.circleId, postId)) {
                        is Success -> showToastAndRefresh(MESSAGE_SUCCESS_REQUEST_REMOVE_POST)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun deleteCirclePost(
            circleId: Int,
            postId: Int,
        ) = withContext(dispatcher) {
            repository.deleteCirclePost(circleId, postId)
        }

        override fun requestReportPost(
            postId: Int,
            reportMessage: String,
        ) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    if (!checkMessageFormat(reportMessage)) return@launch
                    _event.emit(Event.ShowProcessing)

                    when (val result = postReportCirclePost(circleDetail.circleId, postId, reportMessage)) {
                        is Success -> showToast(MESSAGE_SUCCESS_REQUEST_REPORT)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun postReportCirclePost(
            circleId: Int,
            postId: Int,
            message: String,
        ) = withContext(dispatcher) {
            repository.postReportCirclePost(circleId, postId, message)
        }

        private suspend fun showToastAndRefresh(message: String) {
            showToast(message)
            refresh()
        }

        private suspend fun showToast(message: String) {
            _event.emit(Event.ShowToast(message))
        }

        private suspend fun whenUserRequestFail(result: Error<Unit>) {
            if (result.isAuthenticationError()) {
                _event.emit(Event.ShowToast(result.message()))
                _event.emit(Event.SendToLoginScreen)
                return
            }

            _event.emit(Event.ShowDialog(result.message()))
        }

        private suspend fun checkMessageFormat(message: String): Boolean {
            val result = Validator.checkMessage(message)

            return if (result is Invalid) {
                _event.emit(Event.ShowDialog(result.message()))
                false
            } else {
                true
            }
        }

        override fun saveScrollState(scrollState: Parcelable?) {
            _currentScrollState = scrollState
        }

        // 현재는 공지사항용 핀 고정 기능이고, 나중에 게시글 고정 기능 추가 시 사용
        override fun togglePin(post: PostModel) {}

        companion object {
            private const val MESSAGE_SUCCESS_REQUEST_REMOVE_POST = "게시글이 삭제됐어요"
            private const val MESSAGE_SUCCESS_REQUEST_REPORT = "신고 요청이 완료됐어요"
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class CircleDetailPostScreen {
    val hasCollected: Boolean
        get() = _hasCollected
    private var _hasCollected = false

    fun notifyCollected() {
        _hasCollected = true
    }

    data class SuccessView(val posts: Models<PostModel>) : CircleDetailPostScreen()

    data object LoadingView : CircleDetailPostScreen()

    data object ErrorView : CircleDetailPostScreen()
}
