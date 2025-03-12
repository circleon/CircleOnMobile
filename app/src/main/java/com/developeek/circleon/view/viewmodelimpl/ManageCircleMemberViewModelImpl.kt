package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.viewmodel.ManageCircleMemberViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ManageCircleMemberViewModelImpl.ManageCircleMemberViewModelFactory::class)
class ManageCircleMemberViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circle") private val circle: CircleDetailModel,
        @Assisted("members") private val origin: MemberModels,
        @Assisted("membershipStatus") private val membershipStatus: MembershipStatus,
        private val repository: CircleRepository,
    ) : ManageCircleMemberViewModel, ViewModel() {
        @AssistedFactory interface ManageCircleMemberViewModelFactory {
            fun create(
                @Assisted("circle") circle: CircleDetailModel,
                @Assisted("members") members: MemberModels,
                @Assisted("membershipStatus") membershipStatus: MembershipStatus,
            ): ManageCircleMemberViewModelImpl
        }

        override val state: LiveData<UiState>
            get() = _state
        private val _state = MutableLiveData<UiState>()

        override val members: MemberModels
            get() = _members
        private var _members = origin
        private var editCircleMemberRoleJob: Job? = null
        private var banCircleMemberJob: Job? = null
        private var editMembershipStatusJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        override lateinit var error: String

        override fun editCircleMemberRole(
            member: MemberModel,
            role: Role,
        ) {
            editCircleMemberRoleJob?.let {
                if (!it.isCompleted) return
            }

            _state.postValue(UiState.Loading)

            editCircleMemberRoleJob =
                viewModelScope.launch {
                    val result = repository.putCircleMemberRole(circle.id, member.id, role)

                    if (result is Success) {
                        _state.postValue(fetchCircleMembers(currentPage, SIZE_BY_PAGE))
                    } else {
                        error = (result as Error).message()
                        if (result.isAuthenticationError()) {
                            _state.postValue(UiState.AuthenticationError)
                        } else {
                            _state.postValue(UiState.ServiceError)
                        }
                    }
                }
        }

        private suspend fun fetchCircleMembers(
            page: Int,
            size: Int,
        ): UiState {
            val result = repository.getCircleMembers(circle.id, page, size)

            if (result is Success) {
                _members = result.data
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        override fun banCircleMember(member: MemberModel) {
            banCircleMemberJob?.let {
                if (!it.isCompleted) return
            }

            _state.postValue(UiState.Loading)

            banCircleMemberJob =
                viewModelScope.launch {
                    val result = repository.deleteCircleMember(circle.id, member.id)

                    if (result is Success) {
                        _state.postValue(fetchCircleMembers(currentPage, SIZE_BY_PAGE))
                    } else {
                        error = (result as Error).message()
                        if (result.isAuthenticationError()) {
                            _state.postValue(UiState.AuthenticationError)
                        } else {
                            _state.postValue(UiState.ServiceError)
                        }
                    }
                }
        }

        override fun acceptJoinRequest(member: MemberModel) {
            editMembershipStatus(member, MembershipStatus.JOINED) { page, size ->
                fetchCircleJoinRequestedMembers(page, size)
            }
        }

        override fun rejectJoinRequest(member: MemberModel) {
            editMembershipStatus(member, MembershipStatus.NOT_JOINED) { page, size ->
                fetchCircleJoinRequestedMembers(page, size)
            }
        }

        override fun acceptLeaveRequest(member: MemberModel) {
            editMembershipStatus(member, MembershipStatus.NOT_JOINED) { page, size ->
                fetchCircleLeaveRequestedMembers(page, size)
            }
        }

        private fun editMembershipStatus(
            member: MemberModel,
            status: MembershipStatus,
            afterFetch: suspend (Int, Int) -> UiState,
        ) {
            editMembershipStatusJob?.let {
                if (!it.isCompleted) return
            }

            _state.postValue(UiState.Loading)

            editMembershipStatusJob =
                viewModelScope.launch {
                    val result = repository.putCircleMemberStatus(circle.id, member.id, status)

                    if (result is Success) {
                        _state.postValue(afterFetch(currentPage, SIZE_BY_PAGE))
                    } else {
                        error = (result as Error).message()
                        if (result.isAuthenticationError()) {
                            _state.postValue(UiState.AuthenticationError)
                        } else {
                            _state.postValue(UiState.ServiceError)
                        }
                    }
                }
        }

        private suspend fun fetchCircleJoinRequestedMembers(
            page: Int,
            size: Int,
        ): UiState {
            val result = repository.getCircleJoinRequestedMembers(circle.id, page, size)

            if (result is Success) {
                _members = result.data
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        private suspend fun fetchCircleLeaveRequestedMembers(
            page: Int,
            size: Int,
        ): UiState {
            val result = repository.getCircleLeaveRequestedMembers(circle.id, page, size)

            if (result is Success) {
                _members = result.data
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        companion object {
            private const val SIZE_BY_PAGE = 200 // 멤버 데이터 일괄 호출
            private const val DEFAULT_PAGE = 0
        }
    }
