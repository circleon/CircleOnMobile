package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.home.SearchCircleViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModelImpl
    @Inject
    constructor(private val repository: CircleRepository) : SearchCircleViewModel, ViewModel() {
        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<SearchCircleScreen>(SearchCircleScreen.Loading)
        override val screenFlow: StateFlow<SearchCircleScreen> = _screenFlow

        private lateinit var circleSummaries: Models<CircleSummaryModel>
        private var keyword = Const.EMPTY_TEXT

        private var fetchCircleSummariesJob: Job? = null
        private val dispatcher = Dispatchers.IO

        init {
            fetchCircleSummaries()
        }

        override fun refresh() {
            fetchCircleSummaries()
        }

        private fun fetchCircleSummaries() {
            fetchCircleSummariesJob?.let {
                if (!it.isCompleted) return
            }

            fetchCircleSummariesJob =
                viewModelScope.launch {
                    _screenFlow.emit(SearchCircleScreen.Loading)

                    when (val result = getCircleSummaries()) {
                        is Success -> whenFetchCircleSummariesSuccess(result)
                        is Error -> whenFetchCircleSummariesFail(result)
                    }
                }
        }

        private suspend fun getCircleSummaries() =
            withContext(dispatcher) {
                repository.getCircleSummaries()
            }

        private suspend fun whenFetchCircleSummariesSuccess(result: Success<Models<CircleSummaryModel>>) {
            result.data.let {
                circleSummaries = it
                findAndNotify(circleSummaries, keyword)
            }
        }

        private suspend fun whenFetchCircleSummariesFail(result: Error<Models<CircleSummaryModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(SearchCircleScreen.Error)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun setKeywordAndFind(keyword: String) {
            this.keyword = keyword
            if (!::circleSummaries.isInitialized) return

            viewModelScope.launch {
                findAndNotify(circleSummaries, keyword)
            }
        }

        override fun clearKeyword() {
            this.keyword = Const.EMPTY_TEXT
            if (!::circleSummaries.isInitialized) return

            viewModelScope.launch {
                findAndNotify(circleSummaries, keyword)
            }
        }

        private suspend fun findAndNotify(
            circleSummaries: Models<CircleSummaryModel>,
            keyword: String,
        ) {
            if (keyword == Const.EMPTY_TEXT) {
                _screenFlow.emit(SearchCircleScreen.Success(Models()))
            } else {
                _screenFlow.emit(
                    SearchCircleScreen.Success(
                        CircleSummaryModel.findByKeyword(circleSummaries, keyword),
                    ),
                )
            }
        }
    }

sealed class SearchCircleScreen {
    data class Success(val circles: Models<CircleSummaryModel>) : SearchCircleScreen()

    data object Loading : SearchCircleScreen()

    data object Error : SearchCircleScreen()
}
