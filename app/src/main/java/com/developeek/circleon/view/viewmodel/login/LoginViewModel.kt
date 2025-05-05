package com.developeek.circleon.view.viewmodel.login

import com.developeek.circleon.view.viewmodelimpl.login.Event
import kotlinx.coroutines.flow.SharedFlow

interface LoginViewModel {
    val event: SharedFlow<Event>

    fun login(
        email: String,
        password: String,
    )
}
