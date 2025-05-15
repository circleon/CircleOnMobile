package com.developeek.circleon.view.viewmodel.home

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.state.UiState

interface SearchViewModel {
    val state: LiveData<UiState>
    val circles: LiveData<CircleSummaryModels>
    val error: String

    fun refresh()

    fun setKeywordAndFind(keyword: String)

    fun clearKeyword()
}
