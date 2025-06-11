package com.developeek.circleon.view.viewmodel.auth

import androidx.lifecycle.LiveData
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.auth.ChangePasswordScreen
import com.developeek.circleon.view.viewmodelimpl.auth.ChangePasswordScreenEvent
import com.developeek.circleon.view.viewmodelimpl.auth.PasswordChangeManager
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ChangePasswordViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<ChangePasswordScreen>
    val changePasswordScreenEvent: SharedFlow<ChangePasswordScreenEvent>
    val passwordChangeManager: PasswordChangeManager
    val emailAuthenticationTimer: LiveData<Long>

    fun changePassword()

    fun setEmail(email: String)

    fun requestEmailCode()

    fun authenticateEmail(code: String)

    fun setPassword(
        password: String,
        passwordCheck: String,
    )

    fun checkPassword(
        password: String,
        passwordCheck: String,
    )

    fun next()

    fun previous()
}
