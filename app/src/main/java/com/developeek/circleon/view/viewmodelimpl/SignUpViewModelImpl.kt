package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Valid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.domain.vo.UserName
import com.developeek.circleon.view.viewmodel.SignUpViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

        override val emailAuthenticationTimer: LiveData<Long>
            get() = timer
        private val timer = MutableLiveData(TIMER_INIT)

        private var name: UserName? = null
        private var email: UserEmail? = null
        private lateinit var requestEmailCodeJob: Job
        private var emailCode: String? = null
        private lateinit var countEmailCodeExpirationTimeJob: Job
        private var password: Password? = null
        private var passwordMatched: Boolean = false
        private var emailAuthenticated: Boolean = false
        private lateinit var authenticateEmailJob: Job
        private lateinit var signUpJob: Job

        override lateinit var error: String

        override fun signUp() {
            if ((!::signUpJob.isInitialized || signUpJob.isCompleted) && signUpCondition()) {
                signUpJob =
                    viewModelScope.launch {
                        val result = repository.signUp(email!!, name!!, password!!)

                        if (result is Success) {
                            validationMessage.postValue(SIGN_UP_COMPLETED)
                        } else {
                            error = (result as Error).message()
                            uiState.postValue(UiState.ServiceError)
                        }
                    }
            }
        }

        private fun signUpCondition() =
            name != null && email != null && password != null &&
                emailAuthenticated && passwordMatched

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
            if ((!::requestEmailCodeJob.isInitialized || requestEmailCodeJob.isCompleted) && email != null) {
                requestEmailCodeJob =
                    viewModelScope.launch {
                        val result = repository.requestEmailAuthenticationCode(email!!)

                        if (result is Success) {
                            startEmailAuthenticationTimer()
                            validationMessage.postValue(EMAIL_CODE_REQUESTED)
                        } else {
                            error = (result as Error).message()
                            uiState.postValue(UiState.ServiceError)
                        }
                    }
            }
        }

        private fun startEmailAuthenticationTimer() {
            if (::countEmailCodeExpirationTimeJob.isInitialized) countEmailCodeExpirationTimeJob.cancel()

            countEmailCodeExpirationTimeJob =
                viewModelScope.launch {
                    withContext(Dispatchers.Default) {
                        timer.postValue(TIMER_INIT)
                        var time = 0L

                        while (time < TIMER_INIT) {
                            delay(TIMER_INTERVAL)
                            time += TIMER_INTERVAL
                            timer.postValue(TIMER_INIT - time)
                        }
                    }
                }
        }

        override fun setEmailCode(code: String) {
            this.emailCode = code
        }

        override fun authenticateEmail() {
            if ((!::authenticateEmailJob.isInitialized || authenticateEmailJob.isCompleted) &&
                email != null && emailCode != null
            ) {
                authenticateEmailJob =
                    viewModelScope.launch {
                        val result = repository.authenticateEmail(email!!, emailCode!!)

                        if (result is Success) {
                            emailAuthenticated = true
                            countEmailCodeExpirationTimeJob.cancel()
                            validationMessage.postValue(EMAIL_AUTHENTICATED)
                        } else {
                            emailAuthenticated = false
                            error = (result as Error).message()
                            uiState.postValue(UiState.ServiceError)
                        }
                    }
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

        override fun checkPassword(
            password: String,
            passwordCheck: String,
        ) {
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
            private const val NAME_VALIDATED = "1"
            private const val EMAIL_VALIDATED = "2"
            private const val EMAIL_CODE_REQUESTED = "3"
            private const val EMAIL_AUTHENTICATED = "4"
            private const val PASSWORD_VALIDATED = "5"
            private const val PASSWORD_CHECK_VALIDATED = "6"
            private const val SIGN_UP_COMPLETED = "7"

            private const val TIMER_INIT = 300000L
            private const val TIMER_INTERVAL = 1000L
        }
    }
