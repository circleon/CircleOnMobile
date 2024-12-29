package com.developeek.circleon.view.viewmodelimpl

import android.os.Parcelable
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
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
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
        private var circleLoadingJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        override val scrollOver: LiveData<Boolean>
            get() = scrollOverCompleted
        private var scrollOverCompleted = MutableLiveData<Boolean>()
        private var scrollOverLoadingJob: Job? = null
        override val scrollListener = RecyclerViewInfiniteScrollListener()
        override val currentScrollState: Parcelable?
            get() = scrollState
        private var scrollState: Parcelable? = null

        override lateinit var error: String

        init {
            setFilterAndLoad(Category.ALL)
        }

        override fun setFilterAndLoad(category: Category) {
            initCategoryFiltering(category)

            circleLoadingJob =
                viewModelScope.launch {
                    val result = repository.getCircles(currentPage, SIZE_BY_PAGE, category)

                    if (result is Success) {
                        circleModels = result.data
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

        private fun initCategoryFiltering(category: Category) {
            circleLoadingJob?.cancel()
            scrollOverLoadingJob?.cancel()
            categoryFilter.postValue(category)
            uiState.postValue(UiState.Loading)
            currentPage = DEFAULT_PAGE
        }

        override fun scrollOver() {
            scrollOverLoadingJob?.cancel()

            scrollOverLoadingJob =
                viewModelScope.launch {
                    val result = repository.getCircles(currentPage + 1, SIZE_BY_PAGE, categoryFilter.value!!)

                    if (result is Success) {
                        circleModels =
                            circleModels.addAll(result.data).also {
                                if (result.data.isLastPage()) {
                                    it.setAsLast()
                                }
                            }
                        currentPage++
                        scrollOverCompleted.postValue(true)
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

        override fun saveScrollState(scrollState: Parcelable?) {
            this.scrollState = scrollState
        }

        override fun removeScrollState() {
            this.scrollState = null
        }

        override fun refresh() {
            loadByUiState()
        }

        private fun loadByUiState() {
            if (uiState.value == UiState.Success) {
                categoryFilter.postValue(categoryFilter.value)
            } else {
                if (categoryFilter.value == null) {
                    setFilterAndLoad(Category.ALL)
                } else {
                    setFilterAndLoad(categoryFilter.value!!)
                }
            }
        }

        companion object {
            private const val SIZE_BY_PAGE = 10
            private const val DEFAULT_PAGE = 0
        }
    }
