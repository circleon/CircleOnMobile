package com.developeek.circleon.view.viewmodel.login

import com.developeek.circleon.view.viewmodelimpl.login.LoginEvent
import kotlinx.coroutines.flow.SharedFlow

interface LoginViewModel {
    val event: SharedFlow<LoginEvent>

    fun login(
        email: String,
        password: String,
    )
}
