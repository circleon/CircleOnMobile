package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Valid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.domain.vo.Email
import com.developeek.circleon.domain.vo.Name
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.view.viewmodel.SignUpViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignUpViewModelImpl
    @Inject
    constructor(private val repository: LoginRepository) : SignUpViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override val validation: LiveData<String>
            get() = validationMessage
        private val validationMessage = MutableLiveData<String>()

        private var name: Name? = null
        private var email: Email? = null
        private var emailCode: String? = null
        private var password: Password? = null
        private var passwordMatched: Boolean = false
        private var emailAuthenticated: Boolean = false

        private var authenticationCodeJob: Job? = null
        private var authenticationJob: Job? = null
        private var signUpJob: Job? = null

        override var error =
            Const.EMPTY_TEXT

        override fun signUp() {
            TODO("Not yet implemented")
        }

        override fun setName(name: String) {
            val result = Validator.checkName(name)

            if (result is Valid) {
                this.name = result.data
                validationMessage.postValue(NAME_VALIDATED)
            } else {
                this.name = null
                validationMessage.postValue((result as Invalid).message())
            }
        }

        override fun setEmail(email: String) {
            emailAuthenticated = false
            val result = Validator.checkEmail(email)

            if (result is Valid) {
                this.email = result.data
                validationMessage.postValue(EMAIL_VALIDATED)
            } else {
                this.email = null
                validationMessage.postValue((result as Invalid).message())
            }
        }

        override fun requestEmailCode() {
            authenticationCodeJob?.cancel()

            authenticationCodeJob =
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        requestEmailAuthenticationCode()
                    }
                }
        }

        private suspend fun requestEmailAuthenticationCode() {
            val result = repository.requestEmailAuthenticationCode(email!!)

            if (result is Success) {
                validationMessage.postValue(EMAIL_CODE_REQUESTED)
            } else {
                error = (result as Error).message()
                uiState.postValue(UiState.Error)
            }
        }

        override fun setEmailCode(code: String) {
            this.emailCode = code
        }

        override fun authenticateEmail() {
            authenticationJob?.cancel()

            authenticationJob =
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        emailCode?.let {
                            emailAuthenticate(it)
                        }
                    }
                }
        }

        private suspend fun emailAuthenticate(code: String) {
            val result = repository.authenticateEmail(email!!, code)

            if (result is Success) {
                emailAuthenticated = true
                validationMessage.postValue(EMAIL_AUTHENTICATED)
            } else {
                emailAuthenticated = false
                error = (result as Error).message()
                uiState.postValue(UiState.Error)
            }
        }

        override fun setPassword(password: String) {
            val result = Validator.checkPassword(password)

            if (result is Valid) {
                this.password = result.data
                validationMessage.postValue(PASSWORD_VALIDATED)
            } else {
                this.password = null
                validationMessage.postValue((result as Invalid).message())
            }
        }

        override fun setPasswordCheck(passwordCheck: String) {
            val result = Validator.checkPasswordMatch(password, passwordCheck)

            if (result is Valid) {
                this.passwordMatched = true
                validationMessage.postValue(PASSWORD_CHECK_VALIDATED)
            } else {
                this.passwordMatched = false
                validationMessage.postValue((result as Invalid).message())
            }
        }

        companion object {
            const val NAME_VALIDATED = "1"
            const val EMAIL_VALIDATED = "2"
            const val EMAIL_CODE_REQUESTED = "3"
            const val EMAIL_AUTHENTICATED = "4"
            const val PASSWORD_VALIDATED = "5"
            const val PASSWORD_CHECK_VALIDATED = "6"
            const val SIGN_UP_COMPLETED = "7"
        }
    }
