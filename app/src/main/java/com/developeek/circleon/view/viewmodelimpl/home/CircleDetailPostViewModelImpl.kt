package com.developeek.circleon.view.viewmodelimpl.home

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.repository.Page
import com.developeek.circleon.data.repository.UserRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.model.UserModel
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
        private val circleRepository: CircleRepository,
        private val userRepository: UserRepository,
    ) : CircleDetailPostViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailPostViewModelFactory {
            fun create(
                @Assisted("circleDetail") circleDetail: CircleDetailModel,
            ): CircleDetailPostViewModelImpl
        }

        override val user: UserModel by lazy {
            (userRepository.getUser() as Success).data
        }

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow =
            MutableStateFlow<CircleDetailPostScreen>(CircleDetailPostScreen.Loading)
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
        private val ioDispatcher = Dispatchers.IO
        private val defaultDispatcher = Dispatchers.Default

        init {
            fetchPosts(currentPage, SIZE_BY_PAGE)
        }

        /**
         * 로딩 anim 을 활성화한 채로 새로고침
         * 페이지 전체가 초기화되는 경우 사용(ex. 최초 로딩,  SwipeRefresh ..)
         */
        override fun showLoadingAndRefresh() {
            viewModelScope.launch {
                _screenFlow.emit(CircleDetailPostScreen.Loading)
            }
            refresh()
        }

        // TODO: 페이지 유지를 안 하게끔 수정했기 때문에, 게시글 수정 이후 refresh 대신 다른 함수로 업데이트해줘야됨
        override fun refresh() {
            _currentScrollState = null
            fetchPosts(DEFAULT_PAGE, SIZE_BY_PAGE)
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
        ) = withContext(ioDispatcher) {
            circleRepository.getCirclePosts(circleId, page, size)
        }

        private suspend fun whenFetchPostsSuccess(result: Success<Page<PostModel>>) {
            result.data.let {
                _isLastPage = it.isLastPage
                posts = Models(it.content)
                _screenFlow.emit(CircleDetailPostScreen.Success(posts))
            }
        }

        private suspend fun whenFetchPostsFail(result: Error<Page<PostModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(CircleDetailPostScreen.Error)

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
                _screenFlow.emit(CircleDetailPostScreen.Success(posts))
            }
            currentPage++
        }

        private suspend fun whenScrollOverFail(result: Error<Page<PostModel>>) {
            _event.emit(Event.ShowToast(result.message()))

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun updatePostItem(
            post: PostModel,
            content: String,
        ) {
            viewModelScope.launch {
                replacePostContent(post, content)
                _screenFlow.emit(CircleDetailPostScreen.Success(posts))
            }
        }

        private suspend fun replacePostContent(
            post: PostModel,
            content: String,
        ) = withContext(defaultDispatcher) {
            posts = posts.replaceAndGet(post, post.replaceContentAndGet(content))
        }

        override fun delete(post: PostModel) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = deleteCirclePost(circleDetail.circleId, post.id)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> whenDeleteCirclePostSuccess(post)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun deleteCirclePost(
            circleId: Int,
            postId: Int,
        ) = withContext(ioDispatcher) {
            circleRepository.deleteCirclePost(circleId, postId)
        }

        private suspend fun whenDeleteCirclePostSuccess(post: PostModel) {
            deletePost(post)
            showToast(MESSAGE_SUCCESS_REQUEST_REMOVE_POST)
            _screenFlow.emit(CircleDetailPostScreen.Success(posts))
        }

        private suspend fun deletePost(post: PostModel) =
            withContext(defaultDispatcher) {
                posts = posts.deleteAndGet(post)
            }

        override fun deletePostItem(post: PostModel) {
            viewModelScope.launch {
                deletePost(post)
                _screenFlow.emit(CircleDetailPostScreen.Success(posts))
            }
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
                    val result = postReportCirclePost(circleDetail.circleId, postId, reportMessage)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> showToast(MESSAGE_SUCCESS_REQUEST_REPORT)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun postReportCirclePost(
            circleId: Int,
            postId: Int,
            message: String,
        ) = withContext(ioDispatcher) {
            circleRepository.postReportCirclePost(circleId, postId, message)
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
            private const val MESSAGE_SUCCESS_REQUEST_REMOVE_POST = "게시글이 삭제되었어요"
            private const val MESSAGE_SUCCESS_REQUEST_REPORT = "신고 요청이 완료되었어요"
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class CircleDetailPostScreen {
    data class Success(val posts: Models<PostModel>) : CircleDetailPostScreen() {
        val hasCollected: Boolean
            get() = _hasCollected
        private var _hasCollected = false

        fun notifyCollected() {
            _hasCollected = true
        }
    }

    data object Loading : CircleDetailPostScreen()

    data object Error : CircleDetailPostScreen()
}
