package com.developeek.circleon.view.viewmodelimpl.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.AuthRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
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

        override val emailAuthenticationTimer: LiveData<Long>
            get() = _emailAuthenticationTimer
        private val _emailAuthenticationTimer = MutableLiveData(TIMER_INIT)
        private var emailCodeRequested = false

        private var userRequestJob: Job? = null
        private var validateDataJob: Job? = null
        private var countEmailCodeExpirationTimeJob: Job? = null
        private val ioDispatcher = Dispatchers.IO
        private val defaultDispatcher = Dispatchers.Default

        init {
            viewModelScope.launch {
                updateSignUpProcess(currentStep)
            }
        }

        override fun signUp() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _screenFlow.emit(SignUpScreen.LoadingView)

                    when (
                        val result =
                            repository.signUp(
                                userName = signUpManager.name,
                                email = signUpManager.email,
                                password = signUpManager.password,
                            )
                    ) {
                        is Success -> whenSignUpSuccess()
                        is Error -> whenSignUpFail(result)
                    }
                }
        }

        private suspend fun whenSignUpSuccess() {
            _event.emit(Event.ShowToast(MESSAGE_SUCCESS_SIGN_UP))
            _screenFlow.emit(SignUpScreen.SuccessView)
        }

        private suspend fun whenSignUpFail(result: Error<Unit>) {
            _event.emit(Event.ShowDialog(result.message()))
            _screenFlow.emit(SignUpScreen.NormalView)
        }

        override fun setName(name: String) {
            validateDataJob =
                viewModelScope.launch {
                    validateAndSetName(name)
                    updateSignUpProcess(currentStep)
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
                    updateSignUpProcess(currentStep)
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
            countEmailCodeExpirationTimeJob?.let {
                if (!it.isCompleted) it.cancel()
            }

            countEmailCodeExpirationTimeJob =
                viewModelScope.launch {
                    withContext(Dispatchers.Default) {
                        _emailAuthenticationTimer.postValue(TIMER_INIT)
                        var time = 0L

                        while (time < TIMER_INIT) {
                            delay(TIMER_INTERVAL)
                            time += TIMER_INTERVAL

                            if (!isActive) break
                            _emailAuthenticationTimer.postValue(TIMER_INIT - time)
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
                    updateSignUpProcess(currentStep)
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

        override fun setPassword(
            password: String,
            passwordCheck: String,
        ) {
            validateDataJob =
                viewModelScope.launch {
                    validateAndSetPassword(password)
                    setPasswordCheck(password, passwordCheck)
                    updateSignUpProcess(currentStep)
                }
        }

        private suspend fun validateAndSetPassword(password: String) =
            withContext(defaultDispatcher) {
                signUpManager.validateAndSetPassword(password)
            }

        override fun checkPassword(
            password: String,
            passwordCheck: String,
        ) {
            viewModelScope.launch {
                signUpManager.setPasswordCheck(password, passwordCheck)
                updateSignUpProcess(currentStep)
            }
        }

        private suspend fun setPasswordCheck(
            password: String,
            passwordCheck: String,
        ) = withContext(defaultDispatcher) {
            signUpManager.setPasswordCheck(password, passwordCheck)
        }

        override fun toggleAllTermsAgreement() {
            viewModelScope.launch {
                signUpManager.toggleAllTermsAgreement()
                updateSignUpProcess(currentStep)
            }
        }

        override fun toggleServiceTermsAgreement() {
            viewModelScope.launch {
                signUpManager.toggleServiceTermsAgreement()
                updateSignUpProcess(currentStep)
            }
        }

        override fun togglePrivacyPolicyAgreement() {
            viewModelScope.launch {
                signUpManager.togglePrivacyPolicyAgreement()
                updateSignUpProcess(currentStep)
            }
        }

        override fun toggleCommunityRulesAgreement() {
            viewModelScope.launch {
                signUpManager.toggleCommunityRulesAgreement()
                updateSignUpProcess(currentStep)
            }
        }

        override fun next() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    when (currentStep) {
                        SignUpStep.EMAIL -> requestEmailCodeAndGoNext()
                        else -> goNext()
                    }
                }
        }

        private suspend fun requestEmailCodeAndGoNext() {
            if (emailCodeRequested) {
                currentStep = currentStep.getNext()
                updateSignUpProcess(currentStep)
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
            updateSignUpProcess(currentStep)
        }

        private suspend fun goPrevious() {
            currentStep = currentStep.getPrevious()
            updateSignUpProcess(currentStep)
        }

        private suspend fun updateSignUpProcess(step: SignUpStep) {
            _signUpScreenEvent.emit(
                SignUpScreenEvent.UpdateSignUpProcess(
                    step = step,
                    stepCondition = signUpManager.getConditionBySignUpStep(step),
                    validationMessage = signUpManager.getValidationMessageBySignUpStep(step),
                ),
            )
        }

        companion object {
            private const val MESSAGE_SUCCESS_REQUEST_EMAIL_CODE = "인증번호가 전송되었어요"
            private const val MESSAGE_SUCCESS_EMAIL_AUTHENTICATION = "인증에 성공했어요"
            private const val MESSAGE_SUCCESS_SIGN_UP = "회원가입 완료! 반가워요 :D"

            private const val TIMER_INIT = 300_000L
            private const val TIMER_INTERVAL = 1_000L
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
