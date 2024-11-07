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
        private val uiState = MutableLiveData<UiState>(UiState.Loading)

        override val category: List<Category> = Category.entries
        override val selectedCategory: LiveData<Category>
            get() = categoryFilter
        private val categoryFilter = MutableLiveData<Category>()

        override val circles: CircleModels
            get() = circleModels
        private var circleModels = CircleModels.emptyInstance()

        private var circleLoadingJob: Job? = null

        override fun setFilterAndLoad(category: Category) {
            circleLoadingJob?.cancel()
            categoryFilter.postValue(category)

            circleLoadingJob =
                viewModelScope.launch {
                    val result = repository.getCircles(0, SIZE_BY_PAGE, category)

                    if (result is Success) {
                        circleModels = result.data
                        uiState.postValue(UiState.Success)
                    } else {
                        if ((result as Error).isRefreshExpired()) {
                            uiState.postValue(UiState.RefreshExpiration)
                        } else {
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
        }
    }
