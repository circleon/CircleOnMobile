package com.developeek.circleon.view.viewmodel.auth

import androidx.lifecycle.LiveData
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
    val emailAuthenticationTimer: LiveData<Long>

    fun signUp()

    fun setName(name: String)

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

    fun toggleAllTermsAgreement()

    fun toggleServiceTermsAgreement()

    fun togglePrivacyPolicyAgreement()

    fun toggleCommunityRulesAgreement()

    fun next()

    fun previous()
}
