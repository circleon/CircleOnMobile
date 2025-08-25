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

@HiltViewModel(assistedFactory = CircleDetailNoticeViewModelImpl.CircleDetailNoticeViewModelFactory::class)
class CircleDetailNoticeViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circleDetail") private val circleDetail: CircleDetailModel,
        private val circleRepository: CircleRepository,
        private val userRepository: UserRepository,
    ) : CircleDetailPostViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailNoticeViewModelFactory {
            fun create(
                @Assisted("circleDetail") circleDetail: CircleDetailModel,
            ): CircleDetailNoticeViewModelImpl
        }

        override val user: UserModel by lazy {
            (userRepository.getUser() as Success).data
        }

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<CircleDetailPostScreen>(CircleDetailPostScreen.Loading)
        override val screenFlow: StateFlow<CircleDetailPostScreen> = _screenFlow

        override val isLastPage: Boolean
            get() = _isLastPage
        private var _isLastPage = false
        private var currentPage = DEFAULT_PAGE
        private val postSortBy = compareByDescending<PostModel> { it.isPinned }.thenByDescending { it.createdAt }

        private lateinit var posts: Models<PostModel>
        override val currentScrollState: Parcelable?
            get() = _currentScrollState
        private var _currentScrollState: Parcelable? = null

        private var fetchNoticesJob: Job? = null
        private var scrollOverNoticeJob: Job? = null
        private var userRequestJob: Job? = null
        private val ioDispatcher = Dispatchers.IO
        private val defaultDispatcher = Dispatchers.Default

        init {
            fetchNotices(currentPage, SIZE_BY_PAGE)
        }

        override fun showLoadingAndRefresh() {
            viewModelScope.launch {
                _screenFlow.emit(CircleDetailPostScreen.Loading)
            }
            refresh()
        }

        override fun refresh() {
            _currentScrollState = null
            currentPage = DEFAULT_PAGE
            fetchNotices(currentPage, SIZE_BY_PAGE)
        }

        private fun fetchNotices(
            page: Int,
            size: Int,
        ) {
            fetchNoticesJob?.let {
                if (!it.isCompleted) return
            }

            fetchNoticesJob =
                viewModelScope.launch {
                    scrollOverNoticeJob?.cancel()

                    when (val result = getCircleNotices(circleDetail.circleId, page, size)) {
                        is Success -> whenFetchNoticesSuccess(result)
                        is Error -> whenFetchNoticesFail(result)
                    }
                }
        }

        private suspend fun getCircleNotices(
            circleId: Int,
            page: Int,
            size: Int,
        ) = withContext(ioDispatcher) {
            circleRepository.getCircleNotices(circleId, page, size)
        }

        private suspend fun whenFetchNoticesSuccess(result: Success<Page<PostModel>>) {
            result.data.let {
                _isLastPage = it.isLastPage
                posts = Models(it.content)
                _screenFlow.emit(CircleDetailPostScreen.Success(posts))
            }
        }

        private suspend fun whenFetchNoticesFail(result: Error<Page<PostModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(CircleDetailPostScreen.Error)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun scrollOver() {
            scrollOverNoticeJob?.let {
                if (!it.isCompleted) return
            }

            scrollOverNoticeJob =
                viewModelScope.launch {
                    val result = getCircleNotices(circleDetail.circleId, currentPage + 1, SIZE_BY_PAGE)

                    if (!isActive) return@launch // 스크롤 작업 캔슬 시 내용을 업데이트하지 않고 작업 종료
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

        override fun togglePin(post: PostModel) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val toggled = post.togglePinAndGet()
                    val result = putPostPin(circleDetail.circleId, post.id, toggled.isPinned)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> whenTogglePinSuccess(post, toggled)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun putPostPin(
            circleId: Int,
            postId: Int,
            isPinned: Boolean,
        ) = withContext(ioDispatcher) {
            circleRepository.putPostPin(circleId, postId, isPinned)
        }

        private suspend fun whenTogglePinSuccess(
            post: PostModel,
            toggled: PostModel,
        ) {
            val message =
                if (toggled.isPinned) {
                    MESSAGE_SUCCESS_REQUEST_PIN
                } else {
                    MESSAGE_SUCCESS_REQUEST_REMOVE_PIN
                }

            replacePost(post, toggled)
            _event.emit(Event.ShowToast(message))
            _screenFlow.emit(CircleDetailPostScreen.Success(posts))
        }

        private suspend fun replacePost(
            oldPost: PostModel,
            newPost: PostModel,
        ) = withContext(defaultDispatcher) {
            posts = posts.replaceAndGet(oldPost, newPost).sortedWith(postSortBy)
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
            showToast(MESSAGE_SUCCESS_REQUEST_REMOVE_NOTICE)
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

        private suspend fun showToast(message: String) {
            _event.emit(Event.ShowToast(message))
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
                    val result = postReportCircleNotice(circleDetail.circleId, postId, reportMessage)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> _event.emit(Event.ShowToast(MESSAGE_SUCCESS_REQUEST_REPORT))
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun postReportCircleNotice(
            circleId: Int,
            postId: Int,
            message: String,
        ) = withContext(ioDispatcher) {
            circleRepository.postReportCirclePost(circleId, postId, message)
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

        companion object {
            private const val MESSAGE_SUCCESS_REQUEST_PIN = "공지사항이 고정되었어요"
            private const val MESSAGE_SUCCESS_REQUEST_REMOVE_PIN = "공지사항이 고정이 해제되었어요"
            private const val MESSAGE_SUCCESS_REQUEST_REMOVE_NOTICE = "공지사항이 삭제되었어요"
            private const val MESSAGE_SUCCESS_REQUEST_REPORT = "신고 요청이 완료되었어요"
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }
