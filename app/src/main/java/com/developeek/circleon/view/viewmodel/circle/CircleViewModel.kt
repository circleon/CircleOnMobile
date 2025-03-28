package com.developeek.circleon.view.viewmodel.circle

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.state.UiState

interface CircleViewModel {
    val state: LiveData<UiState>
    val myCircles: CircleSummaryModels
    val error: String

    fun refresh()
}
