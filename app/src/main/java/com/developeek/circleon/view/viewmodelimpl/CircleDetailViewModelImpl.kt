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
        private var circleDetailLoadingJob: Job? = null

        override val currentTabPosition: Int
            get() = tabPosition
        private var tabPosition = 0

        override val currentAppBarExpanded: Boolean
            get() = appBarExpanded
        private var appBarExpanded = true

        override lateinit var error: String

        init {
            load()
        }

        override fun load() {
            circleDetailLoadingJob?.cancel()
            uiState.postValue(UiState.Loading)

            circleDetailLoadingJob =
                viewModelScope.launch {
                    val result = repository.getCircleDetail(circleId)

                    if (result is Success) {
                        circleDetail = result.data
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

        override fun setTabPosition(position: Int) {
            tabPosition = position
        }

        override fun setAppBarExpanded(expanded: Boolean) {
            appBarExpanded = expanded
        }
    }
