package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.home.ManageCircleViewModel
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

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<ManageCircleScreen>(ManageCircleScreen.Loading)
        override val screenFlow: StateFlow<ManageCircleScreen> = _screenFlow

        private var fetchMembersJob: Job? = null
        private var userRequestJob: Job? = null
        private val dispatcher = Dispatchers.IO

        private var currentPage = DEFAULT_PAGE

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

            fetchMembersJob =
                viewModelScope.launch {
                    _screenFlow.emit(ManageCircleScreen.Loading)

                    val circleMembers =
                        async {
                            getCircleMembers(circle.circleId, page, size)
                        }
                    val joinRequestedMembers =
                        async {
                            getJoinRequestedMembersWithMessage(circle.circleId, page, size)
                        }
                    val leaveRequestedMembers =
                        async {
                            getLeaveRequestedMembersWithMessage(circle.circleId, page, size)
                        }

                    awaitAll(circleMembers, joinRequestedMembers, leaveRequestedMembers)
                        .find {
                            it is Error
                        }?.let {
                            whenFetchMembersFail(it as Error)
                        } ?: run {
                        whenFetchMembersSuccess(
                            circleMembers = circleMembers.await() as Success,
                            joinRequestedMembers = joinRequestedMembers.await() as Success,
                            leaveRequestedMembers = leaveRequestedMembers.await() as Success,
                        )
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

        private suspend fun whenFetchMembersSuccess(
            circleMembers: Success<Models<MemberModel>>,
            joinRequestedMembers: Success<Models<MemberModel>>,
            leaveRequestedMembers: Success<Models<MemberModel>>,
        ) {
            _screenFlow.emit(
                ManageCircleScreen.Success(
                    circleMembers = circleMembers.data,
                    joinRequestedMembers = joinRequestedMembers.data,
                    leaveRequestedMembers = leaveRequestedMembers.data,
                ),
            )
        }

        private suspend fun whenFetchMembersFail(result: Error<Models<MemberModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(ManageCircleScreen.Error)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun requestOfficialStatus() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = putCircleOfficialStatus(circle.circleId)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> showToastAndRefresh(MESSAGE_SUCCESS_REQUEST_OFFICIAL_STATUS)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun putCircleOfficialStatus(circleId: Int) =
            withContext(dispatcher) {
                repository.putCircleOfficialStatus(circleId)
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

        override fun deleteCircle() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = requestDeleteCircle(circle.id)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> whenDeleteCircleSuccess()
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun whenDeleteCircleSuccess() {
            _event.emit(Event.ShowToast(MESSAGE_SUCCESS_DELETE_CIRCLE))
            _screenFlow.emit(ManageCircleScreen.Success(hasDeleted = true))
        }

        private suspend fun requestDeleteCircle(circleId: Int) =
            withContext(dispatcher) {
                repository.deleteCircle(circleId)
            }

        companion object {
            private const val MESSAGE_SUCCESS_REQUEST_OFFICIAL_STATUS = "동아리 인증 요청이 전송되었어요"
            private const val MESSAGE_SUCCESS_DELETE_CIRCLE = "동아리 삭제가 완료되었어요"
            private const val SIZE_BY_PAGE = 200 // 멤버 데이터 일괄 호출
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class ManageCircleScreen {
    data class Success(
        val circleMembers: Models<MemberModel> = Models(emptyList()),
        val joinRequestedMembers: Models<MemberModel> = Models(emptyList()),
        val leaveRequestedMembers: Models<MemberModel> = Models(emptyList()),
        val hasDeleted: Boolean = false,
    ) : ManageCircleScreen()

    data object Loading : ManageCircleScreen()

    data object Error : ManageCircleScreen()
}
