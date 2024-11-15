package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.viewmodel.HomeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl
    @Inject
    constructor(private val repository: CircleRepository) : HomeViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override val category: List<Category> = Category.entries
        override val selectedCategory: LiveData<Category>
            get() = categoryFilter
        private val categoryFilter = MutableLiveData<Category>()

        override val circles: CircleModels
            get() = circleModels
        private var circleModels = CircleModels.emptyInstance()
        private var currentPage = DEFAULT_PAGE
        override val scrollOver: LiveData<Boolean>
            get() = scrollOverCompleted
        private var scrollOverCompleted = MutableLiveData<Boolean>()

        private var circleLoadingJob: Job? = null
        private var scrollOverLoadingJob: Job? = null

        override var error = Const.EMPTY_TEXT

        override fun setFilterAndLoad(category: Category) {
            circleLoadingJob?.cancel()
            categoryFilter.postValue(category)
            uiState.postValue(UiState.Loading)
            currentPage = DEFAULT_PAGE

            circleLoadingJob =
                viewModelScope.launch {
                    val result = repository.getCircles(currentPage, SIZE_BY_PAGE, category)

                    if (result is Success) {
                        circleModels = result.data
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

        override fun scrollOver() {
            scrollOverLoadingJob?.cancel()

            scrollOverLoadingJob =
                viewModelScope.launch {
                    val result = repository.getCircles(currentPage + 1, SIZE_BY_PAGE, categoryFilter.value!!)

                    if (result is Success) {
                        circleModels = circleModels.add(result.data)
                        currentPage++
                        scrollOverCompleted.postValue(true)
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

        override fun restore() {
            if (categoryFilter.value == null) {
                setFilterAndLoad(Category.ALL)
            } else {
                categoryFilter.postValue(categoryFilter.value)
            }
            uiState.value?.let {
                uiState.postValue(it)
            }
        }

        companion object {
            private const val SIZE_BY_PAGE = 10
            private const val DEFAULT_PAGE = 0
        }
    }
