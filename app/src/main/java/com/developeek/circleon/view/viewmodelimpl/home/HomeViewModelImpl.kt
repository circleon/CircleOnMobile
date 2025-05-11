package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CategoryModels
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.view.viewmodel.home.HomeViewModel
import com.developeek.circleon.view.viewmodelimpl.Page
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
        private var circles = CircleModels.empty()

        override val isLastPage: Boolean
            get() = _isLastPage
        private var _isLastPage = false
        private var currentPage = DEFAULT_PAGE

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

        private suspend fun whenFetchCirclesSuccess(result: Success<Page<CircleModel>>) {
            circles = CircleModels(result.data.content)
            _isLastPage = result.data.isLastPage
            _event.emit(HomeEvent.ShowSuccessView(circles))
        }

        private suspend fun whenFetchCirclesError(result: Error<Page<CircleModel>>) {
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

        private suspend fun whenScrollOverSuccess(result: Success<Page<CircleModel>>) {
            circles = circles.addAllAndGet(result.data.content)
            _isLastPage = result.data.isLastPage
            currentPage++

            _event.emit(
                HomeEvent.ShowSuccessView(circles).apply {
                    hasCollected = true // 페이지 로딩의 경우 스크롤 상단 초기화 방지
                },
            )
        }

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class HomeEvent {
    var hasCollected: Boolean = false

    data class ShowSuccessView(val circles: CircleModels) : HomeEvent()

    data object ShowLoadingView : HomeEvent()

    data object ShowErrorView : HomeEvent()

    data object SendToLoginScreen : HomeEvent()

    data class ShowToast(val message: String) : HomeEvent()
}
