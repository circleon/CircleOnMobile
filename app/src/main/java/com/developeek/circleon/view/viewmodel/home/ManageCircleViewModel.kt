package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.ManageCircleScreen
import com.developeek.circleon.view.viewmodelimpl.home.ManageCircleScreenEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ManageCircleViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<ManageCircleScreen>
    val manageCircleScreenEvent: SharedFlow<ManageCircleScreenEvent>

    fun refresh()

    fun requestOfficialStatus()

    fun deleteCircle()
}
