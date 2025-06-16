package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.repository.UserRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.home.CircleDetailViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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
        private val circleRepository: CircleRepository,
        private val userRepository: UserRepository,
    ) : CircleDetailViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailViewModelFactory {
            fun create(circleId: Int): CircleDetailViewModelImpl
        }

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<CircleDetailScreen>(CircleDetailScreen.LoadingView)
        override val screenFlow: StateFlow<CircleDetailScreen> = _screenFlow
        private val _tabFlow = MutableSharedFlow<SelectTab>()
        override val tabFlow: SharedFlow<SelectTab> = _tabFlow

        override val currentTabPosition: Int
            get() = _currentTabPosition
        private var _currentTabPosition = 0

        override val currentAppBarExpanded: Boolean
            get() = _currentAppBarExpanded
        private var _currentAppBarExpanded = true

        private lateinit var circleDetail: CircleDetailModel

        private var fetchCircleDetailJob: Job? = null
        private var userRequestJob: Job? = null
        private val dispatcher = Dispatchers.IO

        init {
            fetchCircleDetail()
        }

        override fun refresh() {
            fetchCircleDetail()
        }

        private fun fetchCircleDetail() {
            fetchCircleDetailJob?.let {
                if (!it.isCompleted) return
            }

            fetchCircleDetailJob =
                viewModelScope.launch {
                    _screenFlow.emit(CircleDetailScreen.LoadingView)

                    val circleDetailResult: Result<CircleDetailModel>
                    val circleMembersResult: Result<Models<MemberModel>>
                    withContext(dispatcher) {
                        val circleDetail =
                            async {
                                circleRepository.getCircleDetail(circleId)
                            }
                        val circleMembers =
                            async {
                                circleRepository.getCircleMembers(circleId, DEFAULT_PAGE, MEMBER_SIZE_BY_PAGE)
                            }

                        circleDetailResult = circleDetail.await()
                        circleMembersResult = circleMembers.await()
                    }

                    if (circleDetailResult is Success && circleMembersResult is Success) {
                        whenFetchCircleDetailAndMembersSuccess(circleDetailResult, circleMembersResult)
                    }
                    if (circleDetailResult is Error) {
                        whenFetchCircleDetailFail(circleDetailResult)
                    } else if (circleMembersResult is Error) {
                        whenFetchCircleMembersFail(circleMembersResult)
                    }
                }
        }

        private suspend fun whenFetchCircleDetailAndMembersSuccess(
            circleDetailResult: Success<CircleDetailModel>,
            circleMembersResult: Success<Models<MemberModel>>,
        ) {
            circleDetail = circleDetailResult.data.copyWith(circleMembersResult.data)
            _screenFlow.emit(CircleDetailScreen.SuccessView(circleDetail))
        }

        private suspend fun whenFetchCircleDetailFail(result: Error<CircleDetailModel>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(CircleDetailScreen.ErrorView)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        private suspend fun whenFetchCircleMembersFail(result: Error<Models<MemberModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(CircleDetailScreen.ErrorView)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun requestJoin(joinMessage: String) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    if (!checkMessageFormat(joinMessage)) return@launch
                    _event.emit(Event.ShowProcessing)

                    when (val result = postMyCircle(circleId, joinMessage)) {
                        is Success -> showToastAndRefresh(MESSAGE_SUCCESS_REQUEST_JOIN)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun postMyCircle(
            circleId: Int,
            message: String,
        ) = withContext(dispatcher) {
            userRepository.postMyCircle(circleId, message)
        }

        override fun requestLeave(leaveMessage: String) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    if (!checkMessageFormat(leaveMessage)) return@launch
                    _event.emit(Event.ShowProcessing)

                    when (val result = postCircleLeaveRequest(circleDetail.memberId, leaveMessage)) {
                        is Success -> showToastAndRefresh(MESSAGE_SUCCESS_REQUEST_LEAVE)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun postCircleLeaveRequest(
            memberId: Int,
            message: String,
        ) = withContext(dispatcher) {
            userRepository.postCircleLeaveRequest(memberId, message)
        }

        override fun requestReport(reportMessage: String) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    if (!checkMessageFormat(reportMessage)) return@launch
                    _event.emit(Event.ShowProcessing)

                    when (val result = postReportCircle(circleId, reportMessage)) {
                        is Success -> showToastAndRefresh(MESSAGE_SUCCESS_REQUEST_REPORT)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun postReportCircle(
            circleId: Int,
            message: String,
        ) = withContext(dispatcher) {
            circleRepository.postReportCircle(circleId, message)
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
            private const val MEMBER_SIZE_BY_PAGE = 200
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class CircleDetailScreen {
    data class SuccessView(val circleDetail: CircleDetailModel) : CircleDetailScreen()

    data object LoadingView : CircleDetailScreen()

    data object ErrorView : CircleDetailScreen()
}

data class SelectTab(
    val circleDetail: CircleDetailModel,
    val tabPosition: Int,
    val reselected: Boolean,
)
