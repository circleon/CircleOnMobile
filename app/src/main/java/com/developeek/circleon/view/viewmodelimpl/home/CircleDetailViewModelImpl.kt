package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.viewmodel.home.CircleDetailViewModel
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = CircleDetailViewModelImpl.CircleDetailViewModelFactory::class)
class CircleDetailViewModelImpl
    @AssistedInject
    constructor(
        @Assisted private val circleId: Int,
        private val repository: CircleRepository,
    ) : CircleDetailViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailViewModelFactory {
            fun create(circleId: Int): CircleDetailViewModelImpl
        }

        private val _event = MutableStateFlow<CircleDetailEvent>(CircleDetailEvent.ShowLoadingView)
        override val event: StateFlow<CircleDetailEvent> = _event
        private val _tabFlow = MutableSharedFlow<SelectTab>()
        override val tabFlow: SharedFlow<SelectTab> = _tabFlow
        private val dispatcher = Dispatchers.IO

        override val currentTabPosition: Int
            get() = _currentTabPosition
        private var _currentTabPosition = 0

        override val currentAppBarExpanded: Boolean
            get() = _currentAppBarExpanded
        private var _currentAppBarExpanded = true

        private lateinit var circleDetail: CircleDetailModel

        private var fetchCircleDetailJob: Job? = null
        private var userRequestJob: Job? = null

        init {
            fetchCircleDetail()
        }

        private fun fetchCircleDetail() {
            fetchCircleDetailJob?.let {
                if (!it.isCompleted) return
            }

            fetchCircleDetailJob =
                viewModelScope.launch {
                    _event.emit(CircleDetailEvent.ShowLoadingView)

                    when (val result = getCircleDetail(circleId)) {
                        is Success -> {
                            whenFetchCircleDetailSuccess(result)
                        }
                        is Error -> {
                            whenFetchCircleDetailFail(result)
                        }
                    }
                }
        }

        private suspend fun getCircleDetail(circleId: Int) =
            withContext(dispatcher) {
                repository.getCircleDetail(circleId)
            }

        private suspend fun whenFetchCircleDetailSuccess(result: Success<CircleDetailModel>) {
            circleDetail = result.data
            _event.emit(CircleDetailEvent.ShowSuccessView(circleDetail))
        }

        private suspend fun whenFetchCircleDetailFail(result: Error<CircleDetailModel>) {
            _event.emit(CircleDetailEvent.ShowToast(result.message()))

            if (result.isAuthenticationError()) {
                _event.emit(CircleDetailEvent.SendToLoginScreen)
            } else {
                _event.emit(CircleDetailEvent.ShowErrorView)
            }
        }

        override fun refresh() {
            fetchCircleDetail()
        }

        override fun requestJoin(joinMessage: String) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            viewModelScope.launch {
                if (!checkMessageFormat(joinMessage)) return@launch
                _event.emit(CircleDetailEvent.ShowLoadingView)

                userRequestJob =
                    launch {
                        when (val result = repository.postMyCircle(circleId, joinMessage)) {
                            is Success -> showToastAndRefresh(MESSAGE_SUCCESS_REQUEST_JOIN)
                            is Error -> whenUserRequestFail(result)
                        }
                    }
            }
        }

        override fun requestLeave(leaveMessage: String) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            viewModelScope.launch {
                if (!checkMessageFormat(leaveMessage)) return@launch
                _event.emit(CircleDetailEvent.ShowLoadingView)

                userRequestJob =
                    launch {
                        when (val result = repository.postCircleLeaveRequest(circleDetail.memberId, leaveMessage)) {
                            is Success -> showToastAndRefresh(MESSAGE_SUCCESS_REQUEST_LEAVE)
                            is Error -> whenUserRequestFail(result)
                        }
                    }
            }
        }

        override fun reportCircle(reportMessage: String) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            viewModelScope.launch {
                if (!checkMessageFormat(reportMessage)) return@launch
                _event.emit(CircleDetailEvent.ShowLoadingView)

                when (val result = repository.postReportCircle(circleId, reportMessage)) {
                    is Success -> showToastAndRefresh(MESSAGE_SUCCESS_REQUEST_REPORT)
                    is Error -> whenUserRequestFail(result)
                }
            }
        }

        private suspend fun checkMessageFormat(message: String): Boolean {
            val result = Validator.checkMessage(message)

            return if (result is Invalid) {
                _event.emit(CircleDetailEvent.ShowDialog(result.message()))
                false
            } else {
                true
            }
        }

        private suspend fun showToastAndRefresh(message: String) {
            _event.emit(CircleDetailEvent.ShowToast(message))
            refresh()
        }

        private suspend fun whenUserRequestFail(result: Error<Unit>) {
            if (result.isAuthenticationError()) {
                _event.emit(CircleDetailEvent.ShowToast(result.message()))
                _event.emit(CircleDetailEvent.SendToLoginScreen)
                return
            }

            _event.emit(CircleDetailEvent.ShowDialog(result.message()))
        }

        override fun selectTab(
            position: Int,
            reselected: Boolean,
        ) {
            _currentTabPosition = position

            viewModelScope.launch {
                if (!::circleDetail.isInitialized) return@launch

                _tabFlow.emit(SelectTab(circleDetail, _currentTabPosition, reselected))
            }
        }

        override fun setAppBarExpanded(expanded: Boolean) {
            _currentAppBarExpanded = expanded
        }

        companion object {
            private const val MESSAGE_SUCCESS_REQUEST_JOIN = "가입 신청이 완료됐어요"
            private const val MESSAGE_SUCCESS_REQUEST_LEAVE = "탈퇴 신청이 완료됐어요"
            private const val MESSAGE_SUCCESS_REQUEST_REPORT = "신고 요청이 완료됐어요"
        }
    }

sealed class CircleDetailEvent {
    val hasCollected: Boolean
        get() = _hasCollected
    private var _hasCollected = false

    fun notifyCollected() {
        _hasCollected = true
    }

    data class ShowSuccessView(val circleDetail: CircleDetailModel) : CircleDetailEvent()

    data object ShowLoadingView : CircleDetailEvent()

    data object ShowErrorView : CircleDetailEvent()

    data class ShowToast(val message: String) : CircleDetailEvent()

    data class ShowDialog(val message: String) : CircleDetailEvent()

    data object SendToLoginScreen : CircleDetailEvent()
}

data class SelectTab(
    val circleDetail: CircleDetailModel,
    val tabPosition: Int,
    val reselected: Boolean,
)
