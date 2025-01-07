package com.developeek.circleon.view.viewmodel

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.state.UiState

interface HomeViewModel {
    val state: LiveData<UiState>
    val category: List<Category>
    val selectedCategory: LiveData<Category>
    val circles: CircleModels
    val scrollOver: LiveData<Boolean>
    val scrollListener: RecyclerView.OnScrollListener
    val currentScrollState: Parcelable?
    val error: String

    fun refresh()

    fun setFilterAndFetch(category: Category)

    fun scrollOver()

    fun saveScrollState(scrollState: Parcelable?)

    fun removeScrollState()
}
