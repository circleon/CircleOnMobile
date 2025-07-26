package com.developeek.circleon.view.viewmodel.circle

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.circle.CircleScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CircleViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<CircleScreen>

    fun refresh()
}
