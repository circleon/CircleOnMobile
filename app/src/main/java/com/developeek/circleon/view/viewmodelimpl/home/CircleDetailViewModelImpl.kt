package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.viewmodel.home.CircleDetailViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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

        override val circleDetailState: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override lateinit var circleDetail: CircleDetailModel
        private var fetchCircleDetailJob: Job? = null
        override val circleDetailInitialized: Boolean
            get() = isCircleDetailInitialized
        private var isCircleDetailInitialized = false

        override val currentTabPosition: Int
            get() = tabPosition
        private var tabPosition = 0

        override val currentAppBarExpanded: Boolean
            get() = appBarExpanded
        private var appBarExpanded = true

        override val requestState: LiveData<UiState>
            get() = _requestState
        private val _requestState = MutableLiveData<UiState>()
        private var reportJob: Job? = null

        override val currentJoinMessage: String
            get() = _currentJoinMessage
        private var _currentJoinMessage = Const.EMPTY_TEXT
        override val currentLeaveMessage: String
            get() = _currentLeaveMessage
        private var _currentLeaveMessage = Const.EMPTY_TEXT
        private var requestJoinLeaveJob: Job? = null

        override lateinit var error: String

        init {
            fetchCircleDetail()
        }

        private fun fetchCircleDetail() {
            fetchCircleDetailJob?.let {
                if (!it.isCompleted) return
            }

            uiState.postValue(UiState.Loading)

            fetchCircleDetailJob =
                viewModelScope.launch {
                    val result = repository.getCircleDetail(circleId)

                    if (result is Success) {
                        circleDetail = result.data
                        if (!isCircleDetailInitialized) isCircleDetailInitialized = true
                        uiState.postValue(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        if (result.isAuthenticationError()) {
                            uiState.postValue(UiState.AuthenticationError)
                        } else {
                            uiState.postValue(UiState.ServiceError)
                        }
                    }
                }
        }

        override fun refresh() {
            fetchCircleDetail()
        }

        override fun setTabPosition(position: Int) {
            tabPosition = position
        }

        override fun setAppBarExpanded(expanded: Boolean) {
            appBarExpanded = expanded
        }

        override fun requestJoin(joinMessage: String) {
            _currentJoinMessage = joinMessage
            if (!isMessageFormat(joinMessage)) return
            requestJoinLeaveJob?.let {
                if (!it.isCompleted) return
            }

            _requestState.postValue(UiState.Loading)

            requestJoinLeaveJob =
                viewModelScope.launch {
                    val result = repository.postMyCircle(circleId, _currentJoinMessage)

                    if (result is Success) {
                        refresh()
                    } else {
                        error = (result as Error).message()
                        if (result.isAuthenticationError()) {
                            _requestState.postValue(UiState.AuthenticationError)
                        } else {
                            _requestState.postValue(UiState.ServiceError)
                        }
                    }
                }
        }

        override fun requestLeave(leaveMessage: String) {
            _currentLeaveMessage = leaveMessage
            if (!isMessageFormat(leaveMessage)) return
            requestJoinLeaveJob?.let {
                if (!it.isCompleted) return
            }

            _requestState.postValue(UiState.Loading)

            requestJoinLeaveJob =
                viewModelScope.launch {
                    val result = repository.postCircleLeaveRequest(circleDetail.memberId, _currentLeaveMessage)

                    if (result is Success) {
                        refresh()
                    } else {
                        error = (result as Error).message()
                        if (result.isAuthenticationError()) {
                            _requestState.postValue(UiState.AuthenticationError)
                        } else {
                            _requestState.postValue(UiState.ServiceError)
                        }
                    }
                }
        }

        private fun isMessageFormat(message: String): Boolean {
            val validation = Validator.checkMessage(message)

            return if (validation is Invalid) {
                error = validation.message()
                _requestState.postValue(UiState.ServiceError)
                false
            } else {
                true
            }
        }

        override fun reportCircle(content: String) {
            reportJob?.let {
                if (!it.isCompleted) return
            }

            _requestState.postValue(UiState.Loading)

            reportJob =
                viewModelScope.launch {
                    val result = repository.postReportCircle(circleId, content)

                    if (result is Success) {
                        _requestState.postValue(UiState.Success)
                    } else {
                        error = (result as Error).message()
                        if (result.isAuthenticationError()) {
                            _requestState.postValue(UiState.AuthenticationError)
                        } else {
                            _requestState.postValue(UiState.ServiceError)
                        }
                    }
                }
        }
    }
