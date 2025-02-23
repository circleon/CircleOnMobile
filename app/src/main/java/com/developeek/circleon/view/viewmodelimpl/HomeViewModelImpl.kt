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
import com.developeek.circleon.domain.model.CategoryModels
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

        override lateinit var categories: CategoryModels

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

        override fun refresh() {
            fetchCircles()
        }

        override fun setFilterAndFetch(category: Category) {
            categories = CategoryModels.selectAndGet(category)
            fetchCircles()
        }

        private fun fetchCircles() {
            fetchCircleJob?.let {
                if (!it.isCompleted) return
            }
            uiState.postValue(UiState.Loading)
            currentPage = DEFAULT_PAGE

            fetchCircleJob =
                viewModelScope.launch {
                    val result = repository.getCircles(currentPage, SIZE_BY_PAGE, categories.selectedOrFirst().category)

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

        override fun scrollOver() {
            scrollOverCircleJob?.let {
                if (!it.isCompleted) return
            }

            scrollOverCircleJob =
                viewModelScope.launch {
                    val result =
                        repository.getCircles(
                            currentPage + 1, SIZE_BY_PAGE, categories.selectedOrFirst().category,
                        )

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

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }
