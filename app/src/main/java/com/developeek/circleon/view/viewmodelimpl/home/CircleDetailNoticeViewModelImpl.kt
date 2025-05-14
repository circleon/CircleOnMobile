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

        private lateinit var posts: PostModels

        private var fetchNoticesJob: Job? = null
        private var pinNoticeJob: Job? = null
        private var deleteNoticeJob: Job? = null
        private var reportNoticeJob: Job? = null
        private var scrollOverNoticeJob: Job? = null
        private var currentPage = DEFAULT_PAGE
        private val dispatcher = Dispatchers.IO

        init {
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
                posts = PostModels(it.content)
                _isLastPage = it.isLastPage
            }
            _screenFlow.emit(CircleDetailPostScreen.SuccessView(posts))
        }

        private suspend fun whenFetchNoticesFail(result: Error<Page<PostModel>>) {
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
            fetchNotices(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
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
            pinNoticeJob?.let {
                if (!it.isCompleted) return
            }

//            pinNoticeJob =
//                viewModelScope.launch {
//                    val result = repository.putPostPin(circleDetail.id, postId, isPinned)
//
//                    if (result is Success) {
//                        noticeState.postValue(UiState.Success)
//                        fetchNotices(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
//                    } else {
//                        error = (result as Error).message()
//                        if (result.isAuthenticationError()) {
//                            noticeState.postValue(UiState.AuthenticationError)
//                        } else {
//                            noticeState.postValue(UiState.ServiceError)
//                        }
//                    }
//                }
        }

        override fun deleteAndFetch(postId: Int) {
            deleteNoticeJob?.let {
                if (!it.isCompleted) return
            }

//            deleteNoticeJob =
//                viewModelScope.launch {
//                    val result = repository.deleteCirclePost(circleDetail.id, postId)
//
//                    if (result is Success) {
//                        noticeState.postValue(UiState.Success)
//                        fetchNotices(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
//                    } else {
//                        error = (result as Error).message()
//                        if (result.isAuthenticationError()) {
//                            noticeState.postValue(UiState.AuthenticationError)
//                        } else {
//                            noticeState.postValue(UiState.ServiceError)
//                        }
//                    }
//                }
        }

        override fun requestReportPost(
            postId: Int,
            reportMessage: String,
        ) {
            reportNoticeJob?.let {
                if (!it.isCompleted) return
            }

//            reportNoticeJob =
//                viewModelScope.launch {
//                    val result = repository.postReportCirclePost(circleDetail.id, postId, reportMessage)
//
//                    if (result is Success) {
//                        noticeState.postValue(UiState.Success)
//                    } else {
//                        error = (result as Error).message()
//                        if (result.isAuthenticationError()) {
//                            noticeState.postValue(UiState.AuthenticationError)
//                        } else {
//                            noticeState.postValue(UiState.ServiceError)
//                        }
//                    }
//                }
        }

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }
