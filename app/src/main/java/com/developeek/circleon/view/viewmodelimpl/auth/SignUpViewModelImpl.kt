package com.developeek.circleon.view.viewmodelimpl.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.AuthRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.auth.SignUpViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignUpViewModelImpl
    @Inject
    constructor(private val repository: AuthRepository) : SignUpViewModel, ViewModel() {
        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<SignUpScreen>(SignUpScreen.NormalView)
        override val screenFlow: StateFlow<SignUpScreen> = _screenFlow
        private val _signUpScreenEvent = MutableSharedFlow<SignUpScreenEvent>()
        override val signUpScreenEvent: SharedFlow<SignUpScreenEvent> = _signUpScreenEvent

        override val signUpManager: SignUpManager = SignUpManager()
        private var currentStep = SignUpStep.entries.first()

        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override val validation: LiveData<String>
            get() = validationMessage
        private val validationMessage = MutableLiveData<String>()

        override val emailAuthenticationTimer: LiveData<Long>
            get() = timer
        private val timer = MutableLiveData(TIMER_INIT)
        private var emailCodeRequested = false

        private var email: UserEmail? = null
        private var requestEmailCodeJob: Job? = null
        private var emailCode: String? = null
        private var password: Password? = null
        private var passwordMatched: Boolean = false
        private var emailAuthenticated: Boolean = false
        private var authenticateEmailJob: Job? = null
        private var signUpJob: Job? = null

        private var userRequestJob: Job? = null
        private var validateDataJob: Job? = null
        private var countEmailCodeExpirationTimeJob: Job? = null
        private val ioDispatcher = Dispatchers.IO
        private val defaultDispatcher = Dispatchers.Default

        override lateinit var error: String

        init {
            viewModelScope.launch {
                updateSignUpProcess()
            }
        }

        override fun signUp() {
//            if (!signUpCondition()) return
            signUpJob?.let {
                if (!it.isCompleted) return
            }

            uiState.postValue(UiState.Loading)

            signUpJob =
                viewModelScope.launch {
                    val result =
                        repository.signUp(
                            userName = signUpManager.name,
                            email = signUpManager.email,
                            password = signUpManager.password,
                        )

                    if (result is Success) {
                        uiState.postValue(UiState.Success)
                        validationMessage.postValue(SIGN_UP_COMPLETED)
                    } else {
                        error = (result as Error).message()
                        uiState.postValue(UiState.ServiceError)
                    }
                }
        }

//        private fun signUpCondition() =
//            name != null && email != null && password != null &&
//                emailAuthenticated && passwordMatched

        override fun setName(name: String) {
            validateDataJob =
                viewModelScope.launch {
                    validateAndSetName(name)
                    updateSignUpProcess()
                }
        }

        private suspend fun validateAndSetName(name: String) =
            withContext(defaultDispatcher) {
                signUpManager.validateAndSetName(name)
            }

        override fun setEmail(email: String) {
            validateDataJob =
                viewModelScope.launch {
                    emailCodeRequested = false // 이메일 인증 초기화
                    validateAndSetEmail(email)
                    updateSignUpProcess()
                }
        }

        private suspend fun validateAndSetEmail(email: String) =
            withContext(defaultDispatcher) {
                signUpManager.validateAndSetEmail(email)
            }

        override fun requestEmailCode() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    when (val result = requestEmailAuthenticationCode(signUpManager.email)) {
                        is Success -> whenRequestEmailAuthenticationCodeSuccess()
                        is Error -> _event.emit(Event.ShowDialog(result.message()))
                    }
                }
        }

        private suspend fun requestEmailAuthenticationCode(email: UserEmail) =
            withContext(ioDispatcher) {
                repository.requestEmailAuthenticationCode(email)
            }

        private suspend fun whenRequestEmailAuthenticationCodeSuccess() {
            startEmailAuthenticationTimer()
            _event.emit(Event.ShowToast(MESSAGE_SUCCESS_REQUEST_EMAIL_CODE))
        }

        private fun startEmailAuthenticationTimer() {
            countEmailCodeExpirationTimeJob =
                viewModelScope.launch {
                    withContext(Dispatchers.Default) {
                        timer.postValue(TIMER_INIT)
                        var time = 0L

                        while (isActive && time < TIMER_INIT) {
                            delay(TIMER_INTERVAL)
                            time += TIMER_INTERVAL
                            timer.postValue(TIMER_INIT - time)
                        }
                    }
                }
        }

        override fun authenticateEmail(code: String) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    when (val result = authenticateEmail(signUpManager.email, code)) {
                        is Success -> whenEmailAuthenticationSuccess()
                        is Error -> _event.emit(Event.ShowDialog(result.message()))
                    }
                    updateSignUpProcess()
                }
        }

        private suspend fun authenticateEmail(
            email: UserEmail,
            code: String,
        ) = withContext(ioDispatcher) {
            repository.authenticateEmail(email, code)
        }

        private suspend fun whenEmailAuthenticationSuccess() {
            _event.emit(Event.ShowToast(MESSAGE_SUCCESS_EMAIL_AUTHENTICATION))
            signUpManager.setAsEmailAuthenticated()
        }

        override fun setPassword(password: String) {
            validateDataJob =
                viewModelScope.launch {
                    validateAndSetPassword(password)
                    updateSignUpProcess()
                }
        }

        private suspend fun validateAndSetPassword(password: String) =
            withContext(defaultDispatcher) {
                signUpManager.validateAndSetPassword(password)
            }

        override fun checkPassword(password: String) {
            viewModelScope.launch {
                signUpManager.setPasswordCheck(password)
                updateSignUpProcess()
            }
        }

        override fun next() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    when (currentStep) {
                        SignUpStep.EMAIL -> goNext()
                        else -> goNext()
                    }
                }
        }

        private suspend fun requestEmailCodeAndGoNext() {
            if (emailCodeRequested) {
                currentStep = currentStep.getNext()
                updateSignUpProcess()
                return
            }

            _event.emit(Event.ShowProcessing)
            when (val result = requestEmailAuthenticationCode(signUpManager.email)) {
                is Success -> {
                    whenRequestEmailAuthenticationCodeSuccess()
                    emailCodeRequested = true
                    goNext()
                }
                is Error -> _event.emit(Event.ShowDialog(result.message()))
            }
        }

        override fun previous() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    goPrevious()
                }
        }

        private suspend fun goNext() {
            currentStep = currentStep.getNext()
            updateSignUpProcess()
        }

        private suspend fun goPrevious() {
            currentStep = currentStep.getPrevious()
            updateSignUpProcess()
        }

        private suspend fun updateSignUpProcess() {
            _signUpScreenEvent.emit(
                SignUpScreenEvent.UpdateSignUpProcess(
                    step = currentStep,
                    stepCondition = signUpManager.getConditionBySignUpStep(currentStep),
                    validationMessage = signUpManager.getValidationMessageBySignUpStep(currentStep),
                ),
            )
        }

        companion object {
            private const val NAME_VALIDATED = "1"
            private const val EMAIL_VALIDATED = "2"
            private const val EMAIL_CODE_REQUESTED = "3"
            private const val EMAIL_AUTHENTICATED = "4"
            private const val PASSWORD_VALIDATED = "5"
            private const val PASSWORD_CHECK_VALIDATED = "6"
            private const val SIGN_UP_COMPLETED = "7"

            private const val MESSAGE_SUCCESS_REQUEST_EMAIL_CODE = "이메일 주소로 인증번호를 전송했어요"
            private const val MESSAGE_SUCCESS_EMAIL_AUTHENTICATION = "인증에 성공했어요"

            private const val TIMER_INIT = 300000L
            private const val TIMER_INTERVAL = 1000L
        }
    }

sealed class SignUpScreen {
    data object SuccessView : SignUpScreen()

    data object LoadingView : SignUpScreen()

    data object NormalView : SignUpScreen()
}

sealed class SignUpScreenEvent {
    data class UpdateSignUpProcess(
        val step: SignUpStep,
        val stepCondition: Boolean,
        val validationMessage: String,
    ) : SignUpScreenEvent()
}

enum class SignUpStep {
    NAME,
    EMAIL,
    EMAIL_AUTHENTICATION,
    PASSWORD,
    TERMS, ;

    fun getIndex() = entries.indexOf(this)

    fun getNext() = if (this == entries.last()) this else entries[entries.indexOf(this) + 1]

    fun getPrevious() = if (this == entries.first()) this else entries[entries.indexOf(this) - 1]
}
