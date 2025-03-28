package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.viewmodel.home.ManageCircleViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ManageCircleViewModelImpl.ManageCircleViewModelFactory::class)
class ManageCircleViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circle") private val circle: CircleDetailModel,
        private val repository: CircleRepository,
    ) : ManageCircleViewModel, ViewModel() {
        @AssistedFactory
        interface ManageCircleViewModelFactory {
            fun create(
                @Assisted("circle") circle: CircleDetailModel,
            ): ManageCircleViewModelImpl
        }

        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override val circleMembers: MemberModels
            get() = circleMemberModels
        private var circleMemberModels = MemberModels.empty()
        override val joinRequestedMembers: MemberModels
            get() = joinRequestedMembersModels
        private var joinRequestedMembersModels = MemberModels.empty()
        override val leaveRequestedMembers: MemberModels
            get() = leaveRequestedMemberModels
        private var leaveRequestedMemberModels = MemberModels.empty()
        private var fetchMembersJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        override lateinit var error: String

        init {
            fetchMembers(currentPage, SIZE_BY_PAGE)
        }

        override fun refresh() {
            fetchMembers(currentPage, SIZE_BY_PAGE)
        }

        private fun fetchMembers(
            page: Int,
            size: Int,
        ) {
            fetchMembersJob?.let {
                if (!it.isCompleted) return
            }

            uiState.postValue(UiState.Loading)

            var tmpState: UiState = UiState.Success
            fetchMembersJob =
                viewModelScope.launch {
                    val fetchCircleMembersJob =
                        async {
                            return@async fetchCircleMembers(page, size)
                        }
                    val fetchCircleJoinRequestedMembersJob =
                        async {
                            return@async fetchCircleJoinRequestedMembers(page, size)
                        }
                    val fetchCircleLeaveRequestedMembersJob =
                        async {
                            val state = fetchCircleLeaveRequestedMembers(page, size)
                            leaveRequestedMemberModels.get().map {
                                launch { fetchCircleLeaveRequestedMemberMessage(it) }
                            }
                            return@async state
                        }

                    val jobs: List<Deferred<UiState>> =
                        listOf(
                            fetchCircleMembersJob,
                            fetchCircleJoinRequestedMembersJob,
                            fetchCircleLeaveRequestedMembersJob,
                        )
                    jobs.map { job ->
                        job.invokeOnCompletion {
                            if (job.isCancelled) {
                                this.cancel()
                            }
                        }
                    }

                    jobs.awaitAll().map {
                        if (it !is UiState.Success) tmpState = it
                    }
                }.apply {
                    invokeOnCompletion {
                        uiState.postValue(tmpState)
                    }
                }
        }

        private suspend fun fetchCircleMembers(
            page: Int,
            size: Int,
        ): UiState {
            val result = repository.getCircleMembers(circle.id, page, size)

            if (result is Success) {
                circleMemberModels = result.data
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        private suspend fun fetchCircleJoinRequestedMembers(
            page: Int,
            size: Int,
        ): UiState {
            val result = repository.getCircleJoinRequestedMembers(circle.id, page, size)

            if (result is Success) {
                joinRequestedMembersModels = result.data
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
                leaveRequestedMemberModels = result.data
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        private suspend fun fetchCircleLeaveRequestedMemberMessage(member: MemberModel) {
            val result = repository.getCircleLeaveRequestedMemberMessage(circle.id, member.id)

            if (result is Success) {
                member.setMessage(result.data)
            }
        }

        companion object {
            private const val SIZE_BY_PAGE = 200 // 멤버 데이터 일괄 호출
            private const val DEFAULT_PAGE = 0
        }
    }
