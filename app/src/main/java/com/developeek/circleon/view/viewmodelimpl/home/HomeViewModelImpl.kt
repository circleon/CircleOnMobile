package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.repository.Page
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CategoryModels
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.home.HomeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl
    @Inject
    constructor(private val repository: CircleRepository) : HomeViewModel, ViewModel() {
        private val _screenFlow = MutableStateFlow<HomeScreen>(HomeScreen.LoadingView)
        override val screenFlow: StateFlow<HomeScreen> = _screenFlow
        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event

        override val categories: CategoryModels
            get() = _categories
        private var _categories = CategoryModels(emptyList())
        private lateinit var circles: Models<CircleModel>

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

            fetchCirclesJob =
                viewModelScope.launch {
                    _screenFlow.emit(HomeScreen.LoadingView)
                    scrollOverCircleJob?.cancel()
                    currentPage = DEFAULT_PAGE // 카테고리를 선택할 때는 circles 를 재사용하지 않기 때문에 currentPage 도 초기화
                    _categories = CategoryModels.selectAndGet(category)

                    when (val result = getCircles(currentPage, SIZE_BY_PAGE, category)) {
                        is Success -> whenFetchCirclesSuccess(result)
                        is Error -> whenFetchCirclesFail(result)
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
            result.data.let {
                circles = Models(it.content)
                _isLastPage = it.isLastPage
                _screenFlow.emit(HomeScreen.SuccessView(circles))
            }
        }

        private suspend fun whenFetchCirclesFail(result: Error<Page<CircleModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(HomeScreen.ErrorView)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun scrollOver() {
            scrollOverCircleJob?.let {
                if (!it.isCompleted) return
            }

            scrollOverCircleJob =
                viewModelScope.launch {
                    val result = getCircles(currentPage + 1, SIZE_BY_PAGE, categories.selectedOrFirst().category)

                    if (!isActive) return@launch // 스크롤 작업 캔슬 시 내용을 업데이트하지 않고 작업 종료
                    when (result) {
                        is Success -> whenScrollOverSuccess(result)
                        is Error -> whenScrollOverFail(result)
                    }
                }
        }

        private suspend fun whenScrollOverSuccess(result: Success<Page<CircleModel>>) {
            result.data.let {
                _isLastPage = it.isLastPage
                circles = circles.addAllAndGet(it.content)
                _screenFlow.emit(
                    HomeScreen.SuccessView(circles).apply {
                        notifyCollected() // 스크롤 초기화 방지를 위해 collect 처리
                    },
                )
            }
            currentPage++
        }

        private suspend fun whenScrollOverFail(result: Error<Page<CircleModel>>) {
            _event.emit(Event.ShowToast(result.message()))

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class HomeScreen {
    val hasCollected: Boolean
        get() = _hasCollected
    private var _hasCollected = false

    fun notifyCollected() {
        _hasCollected = true
    }

    data class SuccessView(val circles: Models<CircleModel>) : HomeScreen()

    data object LoadingView : HomeScreen()

    data object ErrorView : HomeScreen()
}
