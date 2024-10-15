package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.state.UiState

interface SignUpViewModel {
    val state: LiveData<UiState>
    val nameValidation: LiveData<String>
    val emailValidation: LiveData<String>
    val emailDuplication: LiveData<Boolean>
    val emailAuthenticationCodeRequest: LiveData<Boolean>
    val passwordValidation: LiveData<String>
    val passwordCheckValidation: LiveData<String>
    var error: String

    fun setName(name: String)

    fun setEmail(email: String)

    fun checkEmailDuplication()

    fun requestEmailAuthenticationCode()

    fun setEmailCode(code: String)

    fun authenticateEmail()

    fun setPassword(password: String)

    fun setPasswordCheck(passwordCheck: String)

    fun signUp()
}
