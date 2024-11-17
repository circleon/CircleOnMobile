package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.viewmodel.SearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModelImpl
    @Inject
    constructor(private val repository: CircleRepository) : SearchViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>(UiState.Loading)

        override val circles: LiveData<CircleSummaryModels>
            get() = searchResult
        private val searchResult = MutableLiveData<CircleSummaryModels>()
        private lateinit var circleSummaryModels: CircleSummaryModels

        private var keyword = Const.EMPTY_TEXT

        private var loadCircleJob: Job? = null

        override var error = Const.EMPTY_TEXT

        init {
            loadCircles()
        }

        private fun loadCircles() {
            loadCircleJob?.cancel()

            loadCircleJob =
                viewModelScope.launch {
                    val result = repository.getCircleSummaries()

                    if (result is Success) {
                        circleSummaryModels = result.data
                        notifySearchResultByKeyword()
                        uiState.postValue(UiState.Success)
                    } else {
                        if ((result as Error).isRefreshExpired()) {
                            uiState.postValue(UiState.RefreshExpiration)
                        } else {
                            error = result.message()
                            uiState.postValue(UiState.Error)
                        }
                    }
                }
        }

        override fun setKeyword(keyword: String) {
            this.keyword = keyword

            if (::circleSummaryModels.isInitialized) {
                notifySearchResultByKeyword()
            }
        }

        override fun clearKeyword() {
            this.keyword = Const.EMPTY_TEXT

            searchResult.postValue(CircleSummaryModels.emptyInstance())
        }

        private fun notifySearchResultByKeyword() {
            if (keyword == Const.EMPTY_TEXT) {
                searchResult.postValue(CircleSummaryModels.emptyInstance())
            } else {
                searchResult.postValue(circleSummaryModels.find(keyword))
            }
        }
    }
