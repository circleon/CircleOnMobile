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

        override lateinit var error: String

        init {
            loadCircles()
        }

        override fun loadCircles() {
            loadCircleJob?.cancel()

            loadCircleJob =
                viewModelScope.launch {
                    val result = repository.getCircleSummaries()

                    if (result is Success) {
                        circleSummaryModels = result.data
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

        override fun setKeywordAndFind(keyword: String) {
            this.keyword = keyword

            if (::circleSummaryModels.isInitialized) {
                notifySearchResultByKeyword()
            }
            if (uiState.value is UiState.ServiceError) {
                loadCircles()
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
