package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.state.UiState

interface SignUpViewModel {
    val state: LiveData<UiState>
    val validation: LiveData<String>
    var error: String

    fun setName(name: String)

    fun setEmail(email: String)

    fun requestEmailCode()

    fun authenticateEmail(code: String)

    fun setPassword(password: String)

    fun setPasswordCheck(passwordCheck: String)

    fun signUp()
}
