package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.ManageCircleScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ManageCircleViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<ManageCircleScreen>

    fun refresh()

    fun requestOfficialStatus()

    fun deleteCircle()
}
