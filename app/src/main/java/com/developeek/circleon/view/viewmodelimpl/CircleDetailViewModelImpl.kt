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
import kotlinx.coroutines.delay
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

        private var loadingStartTime = 0L
        override lateinit var error: String

        init {
            fetchCircleDetail()
        }

        private fun fetchCircleDetail() {
            fetchCircleDetailJob?.let {
                if (!it.isCompleted) return
            }

            uiState.postValue(UiState.Loading)
            saveLoadingStartTime()

            fetchCircleDetailJob =
                viewModelScope.launch {
                    val result = repository.getCircleDetail(circleId)
                    delay(remainedLoadingTime())

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

        private fun saveLoadingStartTime() {
            this.loadingStartTime = System.currentTimeMillis()
        }

        /**
         * remainedLoadingTime()
         *
         * 코루틴 수행 시 LoadingState 에 머무르는 최소 시간을 계산하여 보장
         */
        private fun remainedLoadingTime(): Long {
            val remainTime = MAX_DEFAULT_ANIM_TIME_MILLIS - (System.currentTimeMillis() - loadingStartTime)

            return if (remainTime > 0) remainTime else 0
        }

        companion object {
            private const val MAX_DEFAULT_ANIM_TIME_MILLIS = 200L
        }
    }
