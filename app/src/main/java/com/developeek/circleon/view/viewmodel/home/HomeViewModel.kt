package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CategoryModels
import com.developeek.circleon.view.viewmodelimpl.home.HomeEvent
import kotlinx.coroutines.flow.StateFlow

interface HomeViewModel {
    val event: StateFlow<HomeEvent>
    val categories: CategoryModels
    val isLastPage: Boolean

    fun refresh()

    fun setFilterAndFetch(category: Category)

    fun scrollOver()
}
