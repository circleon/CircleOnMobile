package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.HomeScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface HomeViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<HomeScreen>
    val isLastPage: Boolean

    fun refresh()

    fun setFilterAndFetch(category: Category)

    fun scrollOver()
}
