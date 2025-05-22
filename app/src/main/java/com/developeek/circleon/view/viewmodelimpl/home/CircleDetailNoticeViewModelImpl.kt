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

@HiltViewModel(assistedFactory = CircleDetailNoticeViewModelImpl.CircleDetailNoticeViewModelFactory::class)
class CircleDetailNoticeViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circleDetail") private val circleDetail: CircleDetailModel,
        private val repository: CircleRepository,
    ) : CircleDetailPostViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailNoticeViewModelFactory {
            fun create(
                @Assisted("circleDetail") circleDetail: CircleDetailModel,
            ): CircleDetailNoticeViewModelImpl
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

        private var fetchNoticesJob: Job? = null
        private var scrollOverNoticeJob: Job? = null
        private var userRequestJob: Job? = null
        private val dispatcher = Dispatchers.IO

        init {
            fetchNotices(currentPage, SIZE_BY_PAGE)
        }

        override fun showLoadingAndRefresh() {
            viewModelScope.launch {
                _screenFlow.emit(CircleDetailPostScreen.LoadingView)
            }
            refresh()
        }

        override fun refresh() {
            fetchNotices(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
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

                    when (val result = getCircleNotices(circleDetail.id, page, size)) {
                        is Success -> whenFetchNoticesSuccess(result)
                        is Error -> whenFetchNoticesFail(result)
                    }
                }
        }

        private suspend fun getCircleNotices(
            circleId: Int,
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            repository.getCircleNotices(circleId, page, size)
        }

        private suspend fun whenFetchNoticesSuccess(result: Success<Page<PostModel>>) {
            result.data.let {
                _isLastPage = it.isLastPage
                posts = Models(it.content)
                _screenFlow.emit(CircleDetailPostScreen.SuccessView(posts))
            }
        }

        private suspend fun whenFetchNoticesFail(result: Error<Page<PostModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(CircleDetailPostScreen.ErrorView)

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
                    val result = getCircleNotices(circleDetail.id, currentPage + 1, SIZE_BY_PAGE)

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

        override fun pinAndFetch(postId: Int) {
            togglePinAndFetch(postId, true)
        }

        override fun removePinAndFetch(postId: Int) {
            togglePinAndFetch(postId, false)
        }

        private fun togglePinAndFetch(
            postId: Int,
            isPinned: Boolean,
        ) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)

                    when (val result = putPostPin(circleDetail.id, postId, isPinned)) {
                        is Success ->
                            showToastAndRefresh(
                                if (isPinned) MESSAGE_SUCCESS_REQUEST_PIN else MESSAGE_SUCCESS_REQUEST_REMOVE_PIN,
                            )
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun putPostPin(
            circleId: Int,
            postId: Int,
            isPinned: Boolean,
        ) = withContext(dispatcher) {
            repository.putPostPin(circleId, postId, isPinned)
        }

        override fun deleteAndFetch(postId: Int) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)

                    userRequestJob =
                        launch {
                            when (val result = deleteCirclePost(circleDetail.id, postId)) {
                                is Success -> showToastAndRefresh(MESSAGE_SUCCESS_REQUEST_REMOVE_NOTICE)
                                is Error -> whenUserRequestFail(result)
                            }
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

                    when (val result = postReportCircleNotice(circleDetail.id, postId, reportMessage)) {
                        is Success -> _event.emit(Event.ShowToast(MESSAGE_SUCCESS_REQUEST_REPORT))
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun postReportCircleNotice(
            circleId: Int,
            postId: Int,
            message: String,
        ) = withContext(dispatcher) {
            repository.postReportCirclePost(circleId, postId, message)
        }

        private suspend fun showToastAndRefresh(message: String) {
            _event.emit(Event.ShowToast(message))
            refresh()
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
            private const val MESSAGE_SUCCESS_REQUEST_PIN = "공지사항이 고정됐어요"
            private const val MESSAGE_SUCCESS_REQUEST_REMOVE_PIN = "공지사항이 고정이 해제됐어요"
            private const val MESSAGE_SUCCESS_REQUEST_REMOVE_NOTICE = "공지사항이 삭제됐어요"
            private const val MESSAGE_SUCCESS_REQUEST_REPORT = "신고 요청이 완료됐어요"
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }
