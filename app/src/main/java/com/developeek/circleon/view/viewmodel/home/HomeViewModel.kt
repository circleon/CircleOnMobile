package com.developeek.circleon.view.viewmodel.home

import android.os.Parcelable
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CategoryModels
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.view.viewmodelimpl.home.HomeEvent
import kotlinx.coroutines.flow.StateFlow

interface HomeViewModel {
    val event: StateFlow<HomeEvent>
    val circles: CircleModels
    val categories: CategoryModels
    val currentScrollState: Parcelable?

    fun refresh()

    fun setFilterAndFetch(category: Category)

    fun scrollOver()

    fun saveScrollState(scrollState: Parcelable?)

    fun removeScrollState()
}
