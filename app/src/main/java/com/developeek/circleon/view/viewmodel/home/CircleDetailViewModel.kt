package com.developeek.circleon.view.viewmodel.home

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState

interface CircleDetailViewModel {
    val circleDetailState: LiveData<UiState>
    val requestState: LiveData<UiState>
    val circleDetail: CircleDetailModel
    val circleDetailInitialized: Boolean
    val currentTabPosition: Int
    val currentAppBarExpanded: Boolean
    val currentJoinMessage: String
    val currentLeaveMessage: String
    val error: String

    fun refresh()

    fun setTabPosition(position: Int)

    fun setAppBarExpanded(expanded: Boolean)

    fun requestJoin(joinMessage: String)

    fun requestLeave(leaveMessage: String)
}
