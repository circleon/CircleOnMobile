package com.developeek.circleon.view.viewmodelimpl.circle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.viewmodel.circle.CircleViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CircleViewModelImpl
    @Inject
    constructor(private val repository: CircleRepository) : CircleViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override val myCircles: CircleSummaryModels
            get() = _myCircles
        private var _myCircles = CircleSummaryModels.empty()
        override val joinRequestedCircles: CircleSummaryModels
            get() = _joinRequestedCircles
        private var _joinRequestedCircles = CircleSummaryModels.empty()
        override val leaveRequestedCircles: CircleSummaryModels
            get() = _leaveRequestedCircles
        private var _leaveRequestedCircles = CircleSummaryModels.empty()
        private var fetchUserCirclesJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        override lateinit var error: String

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

            uiState.postValue(UiState.Loading)

            var tmpState: UiState = UiState.Success
            fetchUserCirclesJob =
                viewModelScope.launch {
                    val fetchMyCirclesJob =
                        async {
                            return@async fetchMyCircles(page, size)
                        }
                    val fetchMyJoinRequestedCirclesJob =
                        async {
                            return@async fetchMyJoinRequestedCircles(page, size)
                        }
                    val fetchMyLeaveRequestedCirclesJob =
                        async {
                            return@async fetchMyLeaveRequestedCircles(page, size)
                        }

                    val jobs: List<Deferred<UiState>> =
                        listOf(
                            fetchMyCirclesJob,
                            fetchMyJoinRequestedCirclesJob,
                            fetchMyLeaveRequestedCirclesJob,
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

        private suspend fun fetchMyCircles(
            page: Int,
            size: Int,
        ): UiState {
            val result = repository.getMyCircles(page, size)

            if (result is Success) {
                _myCircles = result.data
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        private suspend fun fetchMyJoinRequestedCircles(
            page: Int,
            size: Int,
        ): UiState {
            val result = repository.getMyJoinRequestedCircles(page, size)

            if (result is Success) {
                _joinRequestedCircles = result.data
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        private suspend fun fetchMyLeaveRequestedCircles(
            page: Int,
            size: Int,
        ): UiState {
            val result = repository.getMyLeaveRequestedCircles(page, size)

            if (result is Success) {
                _leaveRequestedCircles = result.data
                return UiState.Success
            } else {
                error = (result as Error).message()
                return if (result.isAuthenticationError()) UiState.AuthenticationError else UiState.ServiceError
            }
        }

        companion object {
            private const val SIZE_BY_PAGE = 100 // 동아리 데이터 일괄 호출
            private const val DEFAULT_PAGE = 0
        }
    }
