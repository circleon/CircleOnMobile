package com.developeek.circleon.view.viewmodel.auth

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.auth.LoginScreenEvent
import kotlinx.coroutines.flow.SharedFlow

interface LoginViewModel {
    val event: SharedFlow<Event>
    val loginScreenEvent: SharedFlow<LoginScreenEvent>

    fun login(
        email: String,
        password: String,
    )
}
