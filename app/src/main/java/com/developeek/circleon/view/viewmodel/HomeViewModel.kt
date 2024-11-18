package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener

interface HomeViewModel {
    val state: LiveData<UiState>
    val category: List<Category>
    val selectedCategory: LiveData<Category>
    val circles: CircleModels
    val scrollOver: LiveData<Boolean>
    val scrollListener: RecyclerViewInfiniteScrollListener
    val error: String

    fun setFilterAndLoad(category: Category)

    fun scrollOver()

    fun restore()
}
