package com.developeek.circleon.view.viewmodel

import com.developeek.circleon.view.Event
import kotlinx.coroutines.flow.SharedFlow

interface MainViewModel {
    val event: SharedFlow<Event>

    fun checkUser()
}
