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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        private lateinit var circles: CircleModels

        override val isLastPage: Boolean
            get() = _isLastPage
        private var _isLastPage = false
        private var currentPage = DEFAULT_PAGE

        private var fetchCirclesJob: Job? = null
        private var scrollOverCircleJob: Job? = null
        private val dispatcher = Dispatchers.IO

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
                        when (val result = getCircles(currentPage, SIZE_BY_PAGE, category)) {
                            is Success -> whenFetchCirclesSuccess(result)
                            is Error -> whenFetchCirclesFail(result)
                        }
                    }
            }
        }

        private suspend fun getCircles(
            page: Int,
            size: Int,
            category: Category,
        ) = withContext(dispatcher) {
            repository.getCircles(page, size, category)
        }

        private suspend fun whenFetchCirclesSuccess(result: Success<Page<CircleModel>>) {
            circles = CircleModels(result.data.content)
            _isLastPage = result.data.isLastPage
            _event.emit(HomeEvent.ShowSuccessView(circles))
        }

        private suspend fun whenFetchCirclesFail(result: Error<Page<CircleModel>>) {
            _event.emit(HomeEvent.ShowToast(result.message()))

            if (result.isAuthenticationError()) {
                _event.emit(HomeEvent.SendToLoginScreen)
            } else {
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
                            getCircles(currentPage + 1, SIZE_BY_PAGE, categories.selectedOrFirst().category)

                        if (!isActive) return@launch
                        when (result) {
                            is Success -> whenScrollOverSuccess(result)
                            is Error -> whenFetchCirclesFail(result)
                        }
                    }
            }
        }

        private suspend fun whenScrollOverSuccess(result: Success<Page<CircleModel>>) {
            result.data.let {
                circles = circles.addAllAndGet(it.content)
                _isLastPage = it.isLastPage
            }
            currentPage++

            _event.emit(
                HomeEvent.ShowSuccessView(circles).apply {
                    notifyCollected() // 페이지 로딩의 경우 스크롤 상단 초기화 방지
                },
            )
        }

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class HomeEvent {
    val hasCollected: Boolean
        get() = _hasCollected
    private var _hasCollected = false

    fun notifyCollected() {
        _hasCollected = true
    }

    data class ShowSuccessView(val circles: CircleModels) : HomeEvent()

    data object ShowLoadingView : HomeEvent()

    data object ShowErrorView : HomeEvent()

    data object SendToLoginScreen : HomeEvent()

    data class ShowToast(val message: String) : HomeEvent()
}
