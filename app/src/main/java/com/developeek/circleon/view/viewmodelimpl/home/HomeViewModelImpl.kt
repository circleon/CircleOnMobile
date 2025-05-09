package com.developeek.circleon.view.viewmodelimpl.home

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CategoryModels
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.view.viewmodel.home.HomeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl
    @Inject
    constructor(private val repository: CircleRepository) : HomeViewModel, ViewModel() {
        private val _event = MutableStateFlow<HomeEvent>(HomeEvent.ShowLoadingView)
        override val event: StateFlow<HomeEvent> = _event

        override val categories: CategoryModels
            get() = _categories
        private var _categories = CategoryModels.empty()

        override val circles: CircleModels
            get() = _circles
        private var _circles = CircleModels.empty()
        private var currentPage = DEFAULT_PAGE

        override val currentScrollState
            get() = _currentScrollState
        private var _currentScrollState: Parcelable? = null

        private var fetchCirclesJob: Job? = null
        private var scrollOverCircleJob: Job? = null

        init {
            setFilterAndFetch(Category.ALL)
        }

        override fun refresh() {
            setFilterAndFetch(categories.selectedOrFirst().category)
        }

        override fun setFilterAndFetch(category: Category) {
            fetchCirclesJob?.let {
                if (!it.isCompleted) return
            }
            _categories = CategoryModels.selectAndGet(category)

            viewModelScope.launch {
                _event.emit(HomeEvent.ShowLoadingView)
                scrollOverCircleJob?.cancel()
                currentPage = DEFAULT_PAGE // 카테고리를 선택할 때는 circles 를 재사용하지 않기 때문에 currentPage 도 초기화

                fetchCirclesJob =
                    launch {
                        when (val result = repository.getCircles(currentPage, SIZE_BY_PAGE, category)) {
                            is Success -> {
                                whenFetchCirclesSuccess(result)
                            }
                            is Error -> {
                                whenFetchCirclesError(result)
                            }
                        }
                    }
            }
        }

        private suspend fun whenFetchCirclesSuccess(result: Success<CircleModels>) {
            _circles = result.data
            _event.emit(HomeEvent.ShowSuccessView(_circles))
        }

        private suspend fun whenFetchCirclesError(result: Error<CircleModels>) {
            if (result.isAuthenticationError()) {
                _event.emit(HomeEvent.SendToLoginScreen)
            } else {
                _event.emit(HomeEvent.ShowToast(result.message()))
                _event.emit(HomeEvent.ShowErrorView)
            }
        }

        override fun scrollOver() {
            scrollOverCircleJob?.let {
                if (!it.isCompleted) return
            }

            viewModelScope.launch {
                scrollOverCircleJob =
                    launch {
                        val result =
                            repository.getCircles(
                                currentPage + 1, SIZE_BY_PAGE, categories.selectedOrFirst().category,
                            )

                        if (!isActive) return@launch
                        when (result) {
                            is Success -> whenScrollOverSuccess(result)
                            is Error -> whenFetchCirclesError(result)
                        }
                    }
            }
        }

        private suspend fun whenScrollOverSuccess(result: Success<CircleModels>) {
            _circles =
                _circles.addAll(result.data).also {
                    if (result.data.isLastPage()) {
                        it.setAsLast()
                    }
                }
            currentPage++

            _event.emit(HomeEvent.ShowInfiniteScrollSuccessView(circles))
        }

        override fun saveScrollState(scrollState: Parcelable?) {
            _currentScrollState = scrollState
        }

        override fun removeScrollState() {
            _currentScrollState = null
        }

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class HomeEvent {
    var hasCollected: Boolean = false

    data class ShowSuccessView(val circles: CircleModels) : HomeEvent()

    data class ShowInfiniteScrollSuccessView(val circles: CircleModels) : HomeEvent()

    data object ShowLoadingView : HomeEvent()

    data object ShowErrorView : HomeEvent()

    data object SendToLoginScreen : HomeEvent()

    data class ShowToast(val message: String) : HomeEvent()
}
