package com.developeek.circleon.view.viewmodelimpl.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.UserRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.circle.CircleViewModel
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
import javax.inject.Inject

@HiltViewModel
class CircleViewModelImpl
    @Inject
    constructor(private val repository: UserRepository) : CircleViewModel, ViewModel() {
        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<CircleScreen>(CircleScreen.Normal)
        override val screenFlow: StateFlow<CircleScreen> = _screenFlow

        private var currentPage = DEFAULT_PAGE

        private var fetchUserCirclesJob: Job? = null
        private val dispatcher = Dispatchers.IO

        init {
            fetchUserCircles(currentPage, SIZE_BY_PAGE)
        }

        override fun refresh() {
            fetchUserCircles(currentPage, SIZE_BY_PAGE)
        }

        private fun fetchUserCircles(
            page: Int,
            size: Int,
        ) {
            fetchUserCirclesJob?.let {
                if (!it.isCompleted) return
            }

            fetchUserCirclesJob =
                viewModelScope.launch {
                    _screenFlow.emit(CircleScreen.Loading)

                    val myCircles =
                        async {
                            getMyCircles(page, size)
                        }
                    val myJoinRequestedCircles =
                        async {
                            getMyJoinRequestedCircles(page, size)
                        }
                    val myLeaveRequestedCircles =
                        async {
                            getMyLeaveRequestedCircles(page, size)
                        }

                    awaitAll(myCircles, myJoinRequestedCircles, myLeaveRequestedCircles)
                        .find {
                            it is Error
                        }?.let {
                            whenFetchMyCirclesFail(it as Error)
                        } ?: run {
                        whenFetchMyCirclesSuccess(
                            myCircles = myCircles.await() as Success,
                            joinRequestedCircles = myJoinRequestedCircles.await() as Success,
                            leaveRequestedCircles = myLeaveRequestedCircles.await() as Success,
                        )
                    }
                }
        }

        private suspend fun whenFetchMyCirclesSuccess(
            myCircles: Success<Models<CircleSummaryModel>>,
            joinRequestedCircles: Success<Models<CircleSummaryModel>>,
            leaveRequestedCircles: Success<Models<CircleSummaryModel>>,
        ) {
            _screenFlow.emit(
                CircleScreen.Success(
                    myCircles = myCircles.data,
                    joinRequestedCircles = joinRequestedCircles.data,
                    leaveRequestedCircles = leaveRequestedCircles.data,
                ),
            )
        }

        private suspend fun whenFetchMyCirclesFail(result: Error<Models<CircleSummaryModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(CircleScreen.Normal)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        private suspend fun getMyCircles(
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            repository.getMyCircles(page, size)
        }

        private suspend fun getMyJoinRequestedCircles(
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            repository.getMyJoinRequestedCircles(page, size)
        }

        private suspend fun getMyLeaveRequestedCircles(
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            repository.getMyLeaveRequestedCircles(page, size)
        }

        companion object {
            private const val SIZE_BY_PAGE = 100 // 동아리 데이터 일괄 호출
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class CircleScreen {
    data class Success(
        val myCircles: Models<CircleSummaryModel>,
        val joinRequestedCircles: Models<CircleSummaryModel>,
        val leaveRequestedCircles: Models<CircleSummaryModel>,
    ) : CircleScreen()

    data object Loading : CircleScreen()

    data object Normal : CircleScreen()
}
