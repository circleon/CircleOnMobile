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
import com.developeek.circleon.view.viewmodel.circle.MyCircleViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyCircleViewModelImpl
    @Inject
    constructor(
        private val repository: CircleRepository,
    ) : MyCircleViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override val joinRequestedCircles: CircleSummaryModels
            get() = _joinRequestedCircles
        private var _joinRequestedCircles = CircleSummaryModels.empty()
        private var circleJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        override lateinit var error: String

        override fun cancelJoinRequest(circleId: Int) {
            circleJob?.let {
                if (!it.isCompleted) return
            }

            uiState.postValue(UiState.Loading)

            circleJob =
                viewModelScope.launch {
                    cancelCircleJoinRequest(circleId)
                }
        }

        private suspend fun cancelCircleJoinRequest(circleId: Int) {
            val result = repository.deleteCircleJoinRequest(circleId)

            if (result is Success) {
                fetchMyJoinRequestedCircles(currentPage, SIZE_BY_PAGE)
            } else {
                error = (result as Error).message()
                if (result.isAuthenticationError()) {
                    uiState.postValue(UiState.AuthenticationError)
                } else {
                    uiState.postValue(UiState.ServiceError)
                }
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

        companion object {
            private const val SIZE_BY_PAGE = 100 // 동아리 데이터 일괄 호출
            private const val DEFAULT_PAGE = 0
        }
    }
