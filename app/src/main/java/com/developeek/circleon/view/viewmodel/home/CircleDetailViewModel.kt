package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailEvent
import com.developeek.circleon.view.viewmodelimpl.home.SelectTab
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CircleDetailViewModel {
    val event: StateFlow<CircleDetailEvent>
    val tabFlow: SharedFlow<SelectTab>
    val currentTabPosition: Int
    val currentAppBarExpanded: Boolean

    fun refresh()

    fun requestJoin(joinMessage: String)

    fun requestLeave(leaveMessage: String)

    fun reportCircle(reportMessage: String)

    fun selectTab(
        position: Int,
        reselected: Boolean,
    )

    fun setAppBarExpanded(expanded: Boolean)
}
