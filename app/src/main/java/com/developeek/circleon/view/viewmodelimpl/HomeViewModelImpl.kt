package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.viewmodel.HomeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
        private val categoryFilter = MutableLiveData(Category.ALL)

        override fun setFilter(category: Category) {
            categoryFilter.postValue(category)

            // TODO: repository.loadCircleByCategory
        }
    }
