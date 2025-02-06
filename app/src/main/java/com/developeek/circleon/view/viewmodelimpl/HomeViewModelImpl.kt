package com.developeek.circleon.view.viewmodelimpl

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.model.UserModel
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
    constructor(
        private val userManager: UserManager,
        private val repository: CircleRepository,
    ) : HomeViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override lateinit var user: UserModel

        override val category: List<Category> = Category.entries
        override val selectedCategory: LiveData<Category>
            get() = categoryFilter
        private val categoryFilter = MutableLiveData<Category>()

        override lateinit var circles: CircleModels
        private var fetchCircleJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        override val scrollOver: LiveData<Boolean>
            get() = scrollOverCompleted
        private var scrollOverCompleted = MutableLiveData<Boolean>()
        private var scrollOverCircleJob: Job? = null
        override val scrollListener = RecyclerViewInfiniteScrollListener()
        override val currentScrollState: Parcelable?
            get() = scrollState
        private var scrollState: Parcelable? = null

        override lateinit var error: String

        init {
            val user = userManager.getUser()

            if (user == null) {
                uiState.postValue(UiState.AuthenticationError)
            } else {
                this.user = user
                setFilterAndFetch(Category.ALL)
            }
        }

        override fun setFilterAndFetch(category: Category) {
            initCategoryFiltering(category)

            fetchCircleJob =
                viewModelScope.launch {
                    val result = repository.getCircles(currentPage, SIZE_BY_PAGE, category)

                    if (result is Success) {
                        circles = result.data
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
            fetchCircleJob?.cancel()
            scrollOverCircleJob?.cancel()
            categoryFilter.postValue(category)
            uiState.postValue(UiState.Loading)
            currentPage = DEFAULT_PAGE
        }

        override fun scrollOver() {
            scrollOverCircleJob?.cancel()

            scrollOverCircleJob =
                viewModelScope.launch {
                    val result = repository.getCircles(currentPage + 1, SIZE_BY_PAGE, categoryFilter.value!!)

                    if (result is Success) {
                        circles =
                            circles.addAll(result.data).also {
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
            fetchOrNotByUiState()
        }

        private fun fetchOrNotByUiState() {
            if (uiState.value == UiState.Success) {
                categoryFilter.postValue(categoryFilter.value)
            } else {
                if (categoryFilter.value == null) {
                    setFilterAndFetch(Category.ALL)
                } else {
                    setFilterAndFetch(categoryFilter.value!!)
                }
            }
        }

        companion object {
            private const val SIZE_BY_PAGE = 10
            private const val DEFAULT_PAGE = 0
        }
    }
