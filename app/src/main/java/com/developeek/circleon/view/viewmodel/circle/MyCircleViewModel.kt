package com.developeek.circleon.view.viewmodel.circle

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.circle.MyCircleScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MyCircleViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<MyCircleScreen>

    fun cancelJoinRequest(memberId: Int)
}
