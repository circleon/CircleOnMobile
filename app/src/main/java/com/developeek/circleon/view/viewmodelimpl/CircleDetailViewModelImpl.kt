package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.viewmodel.CircleDetailViewModel
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
        @AssistedFactory interface CircleDetailViewModelFactory {
            fun create(circleId: Int): CircleDetailViewModelImpl
        }

        override val state: LiveData<UiState>
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

        override fun requestJoin() {
            requestJoinLeaveJob?.let {
                if (!it.isCompleted) return
            }

            uiState.postValue(UiState.Loading)

            requestJoinLeaveJob =
                viewModelScope.launch {
                    val result = repository.postMyCircle(circleId)

                    if (result is Success) {
                        refresh()
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

        override fun requestLeave(message: String) {
            if (!isMessageFormat(message)) return
            requestJoinLeaveJob?.let {
                if (!it.isCompleted) return
            }

            uiState.postValue(UiState.Loading)

            requestJoinLeaveJob =
                viewModelScope.launch {
                    val result = repository.postCircleLeaveRequest(circleDetail.memberId, message)

                    if (result is Success) {
                        refresh()
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

        private fun isMessageFormat(message: String): Boolean {
            val validation = Validator.checkMessage(message)

            return if (validation is Invalid) {
                error = validation.message()
                uiState.postValue(UiState.ServiceError)
                false
            } else {
                true
            }
        }
    }
