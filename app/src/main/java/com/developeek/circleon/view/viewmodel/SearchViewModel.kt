package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.state.UiState

interface SearchViewModel {
    val state: LiveData<UiState>
    val circles: LiveData<CircleSummaryModels>
    var error: String

    fun setKeywordAndFind(keyword: String)

    fun clearKeyword()
}
