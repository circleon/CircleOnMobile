package com.developeek.circleon.view.viewmodel.circle

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.state.UiState

interface MyCircleViewModel {
    val state: LiveData<UiState>
    val joinRequestedCircles: CircleSummaryModels
    val error: String

    fun cancelJoinRequest(circleId: Int)
}
