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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CircleDetailViewModelImpl
    @Inject
    constructor(private val repository: CircleRepository) : CircleDetailViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override lateinit var circleDetail: CircleDetailModel

        private var circleDetailLoadingJob: Job? = null

        override lateinit var error: String

        override fun load(circleId: Int) {
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
                        if (result.isRefreshExpired()) {
                            uiState.postValue(UiState.RefreshExpiration)
                        } else {
                            uiState.postValue(UiState.Error)
                        }
                    }
                }
        }
    }
