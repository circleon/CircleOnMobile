package com.developeek.circleon.view.viewmodelimpl.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.UserRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.circle.MyCircleViewModel
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

@HiltViewModel(assistedFactory = MyCircleViewModelImpl.MyCircleViewModelFactory::class)
class MyCircleViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("origin") private val origin: Models<CircleSummaryModel>,
        private val repository: UserRepository,
    ) : MyCircleViewModel, ViewModel() {
        @AssistedFactory
        interface MyCircleViewModelFactory {
            fun create(
                @Assisted("origin") origin: Models<CircleSummaryModel>,
            ): MyCircleViewModelImpl
        }

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<MyCircleScreen>(MyCircleScreen.NormalView)
        override val screenFlow: StateFlow<MyCircleScreen> = _screenFlow

        private var myCircles = origin
        private var currentPage = DEFAULT_PAGE

        private var fetchCirclesJob: Job? = null
        private var userRequestJob: Job? = null
        private val dispatcher = Dispatchers.IO

        init {
            viewModelScope.launch {
                _screenFlow.emit(MyCircleScreen.SuccessView(myCircles))
            }
        }

        private fun refresh() {
            fetchMyJoinRequestedCircles(currentPage, SIZE_BY_PAGE)
        }

        private fun fetchMyJoinRequestedCircles(
            page: Int,
            size: Int,
        ) {
            fetchCirclesJob?.let {
                if (!it.isCompleted) return
            }

            fetchCirclesJob =
                viewModelScope.launch {
                    _screenFlow.emit(MyCircleScreen.LoadingView)

                    when (val result = getMyJoinRequestedCircles(page, size)) {
                        is Success -> whenFetchMyJoinRequestedCirclesSuccess(result)
                        is Error -> whenFetchMyJoinRequestedCirclesFail(result)
                    }
                }
        }

        private suspend fun getMyJoinRequestedCircles(
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            repository.getMyJoinRequestedCircles(page, size)
        }

        private suspend fun whenFetchMyJoinRequestedCirclesSuccess(result: Success<Models<CircleSummaryModel>>) {
            _screenFlow.emit(MyCircleScreen.SuccessView(result.data))
        }

        private suspend fun whenFetchMyJoinRequestedCirclesFail(result: Error<Models<CircleSummaryModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(MyCircleScreen.NormalView)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun cancelJoinRequest(memberId: Int) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)

                    when (val result = deleteCircleJoinRequest(memberId)) {
                        is Success -> showToastAndRefresh(MESSAGE_SUCCESS_CANCEL_JOIN_REQUEST)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun deleteCircleJoinRequest(memberId: Int) =
            withContext(dispatcher) {
                repository.deleteCircleJoinRequest(memberId)
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

        companion object {
            private const val MESSAGE_SUCCESS_CANCEL_JOIN_REQUEST = "가입 신청이 취소되었어요"
            private const val SIZE_BY_PAGE = 100 // 동아리 데이터 일괄 호출
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class MyCircleScreen {
    data class SuccessView(val circles: Models<CircleSummaryModel>) : MyCircleScreen()

    data object LoadingView : MyCircleScreen()

    data object NormalView : MyCircleScreen()
}
