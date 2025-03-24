package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState

interface CircleDetailViewModel {
    val state: LiveData<UiState>
    val circleDetail: CircleDetailModel
    val circleDetailInitialized: Boolean
    val currentTabPosition: Int
    val currentAppBarExpanded: Boolean
    val error: String

    fun refresh()

    fun setTabPosition(position: Int)

    fun setAppBarExpanded(expanded: Boolean)

    fun requestJoin()

    fun requestLeave(leaveMessage: String)
}
