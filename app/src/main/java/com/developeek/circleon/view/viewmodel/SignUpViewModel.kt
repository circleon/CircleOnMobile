package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.state.UiState

interface SignUpViewModel {
    val state: LiveData<UiState>
    val validation: LiveData<String>
    val emailAuthenticationTimer: LiveData<Long>
    var error: String

    fun signUp()

    fun setName(name: String)

    fun setEmail(email: String)

    fun requestEmailCode()

    fun setEmailCode(code: String)

    fun authenticateEmail()

    fun setPassword(password: String)

    fun setPasswordCheck(passwordCheck: String)
}
