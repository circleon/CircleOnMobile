package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.state.UiState

interface HomeViewModel {
    val state: LiveData<UiState>
    val category: List<Category>
    val selectedCategory: LiveData<Category>

    fun setFilter(category: Category)
}
