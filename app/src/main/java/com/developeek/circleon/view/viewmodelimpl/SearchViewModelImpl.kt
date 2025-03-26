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
        private val circleSummaryModels: CircleSummaryModels
            get() = _circleSummaryModels
        private var _circleSummaryModels = CircleSummaryModels.empty()
        private var fetchCircleSummariesJob: Job? = null

        private var keyword = Const.EMPTY_TEXT

        override lateinit var error: String

        init {
            fetchCircleSummaries()
        }

        private fun fetchCircleSummaries() {
            fetchCircleSummariesJob?.let {
                if (!it.isCompleted) return
            }

            fetchCircleSummariesJob =
                viewModelScope.launch {
                    val result = repository.getCircleSummaries()

                    if (result is Success) {
                        _circleSummaryModels = result.data
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
            fetchCircleSummaries()
        }

        override fun setKeywordAndFind(keyword: String) {
            this.keyword = keyword

            if (uiState.value !is UiState.ServiceError) {
                notifySearchResultByKeyword()
            }
        }

        override fun clearKeyword() {
            this.keyword = Const.EMPTY_TEXT

            searchResult.postValue(CircleSummaryModels.empty())
        }

        private fun notifySearchResultByKeyword() {
            if (keyword == Const.EMPTY_TEXT) {
                searchResult.postValue(CircleSummaryModels.empty())
            } else {
                searchResult.postValue(circleSummaryModels.find(keyword))
            }
        }
    }
