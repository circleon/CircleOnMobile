package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.home.ManageCircleMemberViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = ManageCircleMemberViewModelImpl.ManageCircleMemberViewModelFactory::class)
class ManageCircleMemberViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circle") private val circle: CircleDetailModel,
        @Assisted("origin") private val origin: Models<MemberModel>,
        private val repository: CircleRepository,
    ) : ManageCircleMemberViewModel, ViewModel() {
        @AssistedFactory
        interface ManageCircleMemberViewModelFactory {
            fun create(
                @Assisted("circle") circle: CircleDetailModel,
                @Assisted("origin") origin: Models<MemberModel>,
            ): ManageCircleMemberViewModelImpl
        }

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<ManageCircleMemberScreen>(ManageCircleMemberScreen.Loading)
        override val screenFlow: StateFlow<ManageCircleMemberScreen> = _screenFlow

        private var members: Models<MemberModel> = origin

        private var fetchMembersJob: Job? = null
        private var userRequestJob: Job? = null
        private var dispatcher = Dispatchers.IO

        private var currentPage = DEFAULT_PAGE

        init {
            viewModelScope.launch {
                _screenFlow.emit(ManageCircleMemberScreen.Success(members))
            }
        }

        override fun editCircleMemberRole(
            member: MemberModel,
            role: Role,
        ) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = putCircleMemberRole(circle.circleId, member.memberId, role)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success ->
                            showToastAndDoAfter(
                                MESSAGE_SUCCESS_EDIT_MEMBER_ROLE,
                                after = {
                                    fetchCircleMembers(currentPage, SIZE_BY_PAGE)
                                },
                            )
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun putCircleMemberRole(
            circleId: Int,
            memberId: Int,
            role: Role,
        ) = withContext(dispatcher) {
            repository.putCircleMemberRole(circleId, memberId, role)
        }

        private suspend fun showToastAndDoAfter(
            message: String,
            after: () -> Unit,
        ) {
            _event.emit(Event.ShowToast(message))
            after()
        }

        private suspend fun whenUserRequestFail(result: Error<Unit>) {
            if (result.isAuthenticationError()) {
                _event.emit(Event.ShowToast(result.message()))
                _event.emit(Event.SendToLoginScreen)
                return
            }

            _event.emit(Event.ShowDialog(result.message()))
        }

        private fun fetchCircleMembers(
            page: Int,
            size: Int,
        ) {
            fetchMembersJob?.let {
                if (!it.isCompleted) return
            }

            fetchMembersJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = getCircleMembers(circle.circleId, page, size)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> whenFetchMembersSuccess(result)
                        is Error -> whenFetchMembersFail(result)
                    }
                }
        }

        private suspend fun getCircleMembers(
            circleId: Int,
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            repository.getCircleMembers(circleId, page, size)
        }

        private suspend fun whenFetchMembersSuccess(result: Success<Models<MemberModel>>) {
            members = result.data
            _screenFlow.emit(ManageCircleMemberScreen.Success(members))
        }

        private suspend fun whenFetchMembersFail(result: Error<Models<MemberModel>>) {
            _event.emit(Event.ShowToast(result.message()))

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun banCircleMember(member: MemberModel) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = deleteCircleMember(circle.circleId, member.memberId)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success ->
                            showToastAndDoAfter(
                                MESSAGE_SUCCESS_BAN_MEMBER,
                                after = {
                                    fetchCircleMembers(currentPage, SIZE_BY_PAGE)
                                },
                            )
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun deleteCircleMember(
            circleId: Int,
            memberId: Int,
        ) = withContext(dispatcher) {
            repository.deleteCircleMember(circleId, memberId)
        }

        override fun acceptJoinRequest(member: MemberModel) {
            editMembershipStatus(member, MembershipStatus.JOINED, MESSAGE_SUCCESS_ACCEPT_JOIN_REQUEST)
        }

        override fun rejectJoinRequest(member: MemberModel) {
            editMembershipStatus(member, MembershipStatus.NOT_JOINED, MESSAGE_SUCCESS_REJECT_JOIN_REQUEST)
        }

        override fun acceptLeaveRequest(member: MemberModel) {
            editMembershipStatus(member, MembershipStatus.NOT_JOINED, MESSAGE_SUCCESS_ACCEPT_LEAVE_REQUEST)
        }

        private fun editMembershipStatus(
            member: MemberModel,
            membershipStatus: MembershipStatus,
            successMessage: String,
        ) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = putCircleMemberStatus(circle.circleId, member.memberId, membershipStatus)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> {
                            var after = {}
                            if (member.status.isJoinRequested()) {
                                after = {
                                    fetchJoinRequestedMembers(currentPage, SIZE_BY_PAGE)
                                }
                            }
                            if (member.status.isLeaveRequested()) {
                                after = {
                                    fetchLeaveRequestedMembers(currentPage, SIZE_BY_PAGE)
                                }
                            }
                            showToastAndDoAfter(
                                successMessage,
                                after = after,
                            )
                        }
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun putCircleMemberStatus(
            circleId: Int,
            memberId: Int,
            membershipStatus: MembershipStatus,
        ) = withContext(dispatcher) {
            repository.putCircleMemberStatus(circleId, memberId, membershipStatus)
        }

        private fun fetchJoinRequestedMembers(
            page: Int,
            size: Int,
        ) {
            fetchMembersJob?.let {
                if (!it.isCompleted) return
            }

            fetchMembersJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = getJoinRequestedMembersWithMessage(circle.circleId, page, size)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> whenFetchMembersSuccess(result)
                        is Error -> whenFetchMembersFail(result)
                    }
                }
        }

        private suspend fun getJoinRequestedMembersWithMessage(
            circleId: Int,
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            val result = repository.getCircleJoinRequestedMembers(circleId, page, size)

            when (result) {
                is Success -> {
                    result.data.get().map {
                        async {
                            val message = repository.getCircleJoinRequestedMemberMessage(circleId, it.id)

                            if (message is Success) {
                                it.setMessage(message.data)
                            }
                        }
                    }.awaitAll()
                }
            }

            result
        }

        private fun fetchLeaveRequestedMembers(
            page: Int,
            size: Int,
        ) {
            fetchMembersJob?.let {
                if (!it.isCompleted) return
            }

            fetchMembersJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = getLeaveRequestedMembersWithMessage(circle.circleId, page, size)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> whenFetchMembersSuccess(result)
                        is Error -> whenFetchMembersFail(result)
                    }
                }
        }

        private suspend fun getLeaveRequestedMembersWithMessage(
            circleId: Int,
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            val result = repository.getCircleLeaveRequestedMembers(circleId, page, size)

            when (result) {
                is Success -> {
                    result.data.get().map {
                        async {
                            val message = repository.getCircleLeaveRequestedMemberMessage(circleId, it.id)

                            if (message is Success) {
                                it.setMessage(message.data)
                            }
                        }
                    }.awaitAll()
                }
            }

            result
        }

        companion object {
            private const val MESSAGE_SUCCESS_EDIT_MEMBER_ROLE = "역할 수정이 완료되었어요"
            private const val MESSAGE_SUCCESS_BAN_MEMBER = "멤버 추방이 완료되었어요"
            private const val MESSAGE_SUCCESS_ACCEPT_JOIN_REQUEST = "가입 신청이 승인되었어요"
            private const val MESSAGE_SUCCESS_REJECT_JOIN_REQUEST = "가입 신청이 거절되었어요"
            private const val MESSAGE_SUCCESS_ACCEPT_LEAVE_REQUEST = "탈퇴 신청이 승인되었어요"
            private const val SIZE_BY_PAGE = 200 // 멤버 데이터 일괄 호출
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class ManageCircleMemberScreen {
    data class Success(val members: Models<MemberModel>) : ManageCircleMemberScreen()

    data object Loading : ManageCircleMemberScreen()

    data object Error : ManageCircleMemberScreen()
}
