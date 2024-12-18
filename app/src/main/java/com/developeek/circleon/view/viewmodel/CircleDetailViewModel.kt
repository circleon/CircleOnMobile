package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState

interface CircleDetailViewModel {
    val state: LiveData<UiState>
    val circleDetail: CircleDetailModel
    val selected
    val error: String

    fun load(circleId: Int)
}
