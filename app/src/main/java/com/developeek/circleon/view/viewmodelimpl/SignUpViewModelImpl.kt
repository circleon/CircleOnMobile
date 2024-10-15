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
import com.developeek.circleon.domain.utils.InputValidator
import com.developeek.circleon.domain.utils.Invalid
import com.developeek.circleon.domain.utils.Valid
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

        override val nameValidation: LiveData<String>
            get() = nameMessage
        private val nameMessage = MutableLiveData<String>()

        override val emailValidation: LiveData<String>
            get() = emailMessage
        private val emailMessage = MutableLiveData<String>()

        override val emailDuplication: LiveData<Boolean>
            get() = checkEmailDuplication
        private val checkEmailDuplication = MutableLiveData<Boolean>()

        override val emailAuthenticationCodeRequest: LiveData<Boolean>
            get() = checkEmailAuthenticationCodeRequest
        private val checkEmailAuthenticationCodeRequest = MutableLiveData<Boolean>()

        override val passwordValidation: LiveData<String>
            get() = passwordMessage
        private val passwordMessage = MutableLiveData<String>()

        override val passwordCheckValidation: LiveData<String>
            get() = passwordCheckMessage
        private val passwordCheckMessage = MutableLiveData<String>()

        private var name: String? = null
        private var email: String? = null
        private var emailCode = Const.EMPTY_TEXT
        private var emailDuplicated: Boolean = true
        private var emailAuthenticated: Boolean = false
        private var password: String? = null
        private var passwordCheck: String? = null

        private var checkDuplicationJob: Job? = null
        private var authenticationCodeJob: Job? = null
        private var authenticationJob: Job? = null

        override var error =
            Const.EMPTY_TEXT

        override fun setName(name: String) {
            val result = InputValidator.checkName(name)

            if (result is Valid) {
                this.name = name
                nameMessage.postValue(SUCCESS)
            } else {
                if (this.name != null) this.name = null
                nameMessage.postValue((result as Invalid).message())
            }
        }

        override fun setEmail(email: String) {
            emailDuplicated = true // 중복 검사 이후 수정 방지용
            emailAuthenticated = false
            val result = InputValidator.checkEmail(email)

            if (result is Valid) {
                this.email = email
                emailMessage.postValue(SUCCESS)
            } else {
                if (this.email != null) this.email = null
                emailMessage.postValue((result as Invalid).message())
            }
        }

        override fun checkEmailDuplication() {
            checkDuplicationJob?.cancel()

            checkDuplicationJob =
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        emailDuplicationCheck()
                    }
                }
        }

        private suspend fun emailDuplicationCheck() {
            val result = repository.checkEmailDuplication(email!!)

            if (result is Success) {
                this.emailDuplicated = false
                checkEmailDuplication.postValue(false)
            } else {
                this.emailDuplicated = true
                error = (result as Error).message()
                uiState.postValue(UiState.Error)
            }
        }

        override fun requestEmailAuthenticationCode() {
            authenticationCodeJob?.cancel()

            authenticationCodeJob =
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        emailAuthenticationCodeRequest()
                    }
                }
        }

        private suspend fun emailAuthenticationCodeRequest() {
            val result = repository.requestEmailAuthenticationCode(email!!)

            if (result is Success) {
                checkEmailAuthenticationCodeRequest.postValue(true)
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
                        if (email != Const.EMPTY_TEXT) {
                            emailAuthenticate()
                        }
                    }
                }
        }

        private suspend fun emailAuthenticate() {
            val result = repository.authenticateEmail(email!!, emailCode)

            if (result is Success) {
                emailAuthenticated = true
                uiState.postValue(UiState.Success)
            } else {
                emailAuthenticated = false
                error = (result as Error).message()
                uiState.postValue(UiState.Error)
            }
        }

        override fun setPassword(password: String) {
            TODO("Not yet implemented")
        }

        override fun setPasswordCheck(passwordCheck: String) {
            TODO("Not yet implemented")
        }

        override fun signUp() {
            TODO("Not yet implemented")
        }

        companion object {
            private const val SUCCESS = ""
        }
    }
