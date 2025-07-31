package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.repository.Page
import com.developeek.circleon.data.repository.UserRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CategoryModel
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.model.UserModel
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
    constructor(
        private val circleRepository: CircleRepository,
        private val userRepository: UserRepository,
    ) : HomeViewModel, ViewModel() {
        override val user: UserModel by lazy {
            (userRepository.getUser() as Success).data
        }

        private var categories: Models<CategoryModel> = Models()
        private var circles: Models<CircleModel> = Models()

        private val _screenFlow = MutableStateFlow<HomeScreen>(HomeScreen.Loading(categories))
        override val screenFlow: StateFlow<HomeScreen> = _screenFlow
        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event

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
            setFilterAndFetch((categories.get().find { it.isSelected } ?: categories.first()).category)
        }

        override fun setFilterAndFetch(category: Category) {
            fetchCirclesJob?.let {
                if (!it.isCompleted) return
            }

            fetchCirclesJob =
                viewModelScope.launch {
                    scrollOverCircleJob?.cancel()
                    currentPage = DEFAULT_PAGE // 카테고리를 선택할 때는 circles 를 재사용하지 않기 때문에 currentPage 도 초기화
                    categories = CategoryModel.selectAndGet(category)
                    _screenFlow.emit(HomeScreen.Loading(categories))

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
            circleRepository.getCircles(page, size, category)
        }

        private suspend fun whenFetchCirclesSuccess(result: Success<Page<CircleModel>>) {
            result.data.let {
                circles = Models(it.content)
                _isLastPage = it.isLastPage
                _screenFlow.emit(HomeScreen.Success(categories, circles))
            }
        }

        private suspend fun whenFetchCirclesFail(result: Error<Page<CircleModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(HomeScreen.Error(categories))

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
                    val result =
                        getCircles(
                            currentPage + 1,
                            SIZE_BY_PAGE,
                            (categories.get().find { it.isSelected } ?: categories.first()).category,
                        )

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
                    HomeScreen.Success(categories, circles).apply {
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

sealed class HomeScreen(val categories: Models<CategoryModel>) {
    data class Success(
        val currentCategories: Models<CategoryModel>,
        val circles: Models<CircleModel>,
    ) : HomeScreen(currentCategories) {
        val hasCollected: Boolean
            get() = _hasCollected
        private var _hasCollected = false

        fun notifyCollected() {
            _hasCollected = true
        }
    }

    data class Loading(val currentCategories: Models<CategoryModel>) : HomeScreen(currentCategories)

    data class Error(val currentCategories: Models<CategoryModel>) : HomeScreen(currentCategories)
}
