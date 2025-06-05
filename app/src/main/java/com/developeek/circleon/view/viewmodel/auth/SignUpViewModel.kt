package com.developeek.circleon.view.viewmodel.auth

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpManager
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpScreen
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpScreenEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SignUpViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<SignUpScreen>
    val signUpScreenEvent: SharedFlow<SignUpScreenEvent>
    val signUpManager: SignUpManager
    val state: LiveData<UiState>
    val validation: LiveData<String>
    val emailAuthenticationTimer: LiveData<Long>
    val error: String

    fun signUp()

    fun setName(name: String)

    fun setEmail(email: String)

    fun authenticateEmail(code: String)

    fun setPassword(password: String)

    fun checkPassword(password: String)

    fun next()

    fun previous()

    fun requestEmailCode()
}
