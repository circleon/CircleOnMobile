package com.developeek.circleon.view.viewmodelimpl.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.dto.auth.PolicyId
import com.developeek.circleon.data.dto.auth.PublicId
import com.developeek.circleon.data.repository.AuthRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.auth.ChangePasswordViewModel
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
class ChangePasswordViewModelImpl
    @Inject
    constructor(private val repository: AuthRepository) : ChangePasswordViewModel, ViewModel() {
        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<ChangePasswordScreen>(ChangePasswordScreen.NormalView)
        override val screenFlow: StateFlow<ChangePasswordScreen> = _screenFlow
        private val _changePasswordScreenEvent = MutableSharedFlow<ChangePasswordScreenEvent>()
        override val changePasswordScreenEvent: SharedFlow<ChangePasswordScreenEvent> = _changePasswordScreenEvent

        override val passwordChangeManager: PasswordChangeManager = PasswordChangeManager()
        private var currentStep = ChangePasswordStep.entries.first()

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

        override fun changePassword() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _screenFlow.emit(ChangePasswordScreen.LoadingView)

                    when (
                        val result =
                            repository.changePassword(
                                passwordChangeManager.publicId,
                                passwordChangeManager.password,
                            )
                    ) {
                        is Success -> whenChangePasswordSuccess()
                        is Error -> whenChangePasswordFail(result)
                    }
                }
        }

        private suspend fun whenChangePasswordSuccess() {
            _event.emit(Event.ShowToast(MESSAGE_SUCCESS_CHANGE_PASSWORD))
            _screenFlow.emit(ChangePasswordScreen.SuccessView)
        }

        private suspend fun whenChangePasswordFail(result: Error<Unit>) {
            _event.emit(Event.ShowDialog(result.message()))
            _screenFlow.emit(ChangePasswordScreen.NormalView)
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
                passwordChangeManager.validateAndSetEmail(email)
            }

        override fun requestEmailCode() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    when (val result = requestEmailAuthenticationCodeForNewPassword(passwordChangeManager.email)) {
                        is Success -> whenRequestEmailAuthenticationCodeSuccess(result)
                        is Error -> _event.emit(Event.ShowDialog(result.message()))
                    }
                }
        }

        private suspend fun requestEmailAuthenticationCodeForNewPassword(email: UserEmail) =
            withContext(ioDispatcher) {
                repository.requestEmailAuthenticationCodeForNewPassword(email)
            }

        private suspend fun whenRequestEmailAuthenticationCodeSuccess(result: Success<PolicyId>) {
            passwordChangeManager.setPolicyId(result.data)
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
                    when (val result = authenticateEmailForNewPassword(passwordChangeManager.policyId, code)) {
                        is Success -> whenEmailAuthenticationSuccess(result)
                        is Error -> _event.emit(Event.ShowDialog(result.message()))
                    }
                    updateSignUpProcess(currentStep)
                }
        }

        private suspend fun authenticateEmailForNewPassword(
            policyId: PolicyId,
            code: String,
        ) = withContext(ioDispatcher) {
            repository.authenticateEmailForNewPassword(policyId, code)
        }

        private suspend fun whenEmailAuthenticationSuccess(result: Success<PublicId>) {
            passwordChangeManager.setPublicId(result.data)
            _event.emit(Event.ShowToast(MESSAGE_SUCCESS_EMAIL_AUTHENTICATION))
            passwordChangeManager.setAsEmailAuthenticated()
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
                passwordChangeManager.validateAndSetPassword(password)
            }

        override fun checkPassword(
            password: String,
            passwordCheck: String,
        ) {
            viewModelScope.launch {
                passwordChangeManager.setPasswordCheck(password, passwordCheck)
                updateSignUpProcess(currentStep)
            }
        }

        private suspend fun setPasswordCheck(
            password: String,
            passwordCheck: String,
        ) = withContext(defaultDispatcher) {
            passwordChangeManager.setPasswordCheck(password, passwordCheck)
        }

        override fun next() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    when (currentStep) {
                        ChangePasswordStep.EMAIL -> requestEmailCodeAndGoNext()
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
            when (val result = requestEmailAuthenticationCodeForNewPassword(passwordChangeManager.email)) {
                is Success -> {
                    whenRequestEmailAuthenticationCodeSuccess(result)
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

        private suspend fun updateSignUpProcess(step: ChangePasswordStep) {
            _changePasswordScreenEvent.emit(
                ChangePasswordScreenEvent.UpdateChangePasswordProcess(
                    step = step,
                    stepCondition = passwordChangeManager.getConditionByChangePasswordStep(step),
                    validationMessage = passwordChangeManager.getValidationMessageByChangePasswordStep(step),
                ),
            )
        }

        companion object {
            private const val MESSAGE_SUCCESS_REQUEST_EMAIL_CODE = "인증번호가 전송되었어요"
            private const val MESSAGE_SUCCESS_EMAIL_AUTHENTICATION = "인증에 성공했어요"
            private const val MESSAGE_SUCCESS_CHANGE_PASSWORD = "비밀번호가 재설정되었어요"

            private const val TIMER_INIT = 300_000L
            private const val TIMER_INTERVAL = 1_000L
        }
    }

sealed class ChangePasswordScreen {
    data object SuccessView : ChangePasswordScreen()

    data object LoadingView : ChangePasswordScreen()

    data object NormalView : ChangePasswordScreen()
}

sealed class ChangePasswordScreenEvent {
    data class UpdateChangePasswordProcess(
        val step: ChangePasswordStep,
        val stepCondition: Boolean,
        val validationMessage: String,
    ) : ChangePasswordScreenEvent()
}

enum class ChangePasswordStep {
    EMAIL,
    EMAIL_AUTHENTICATION,
    PASSWORD, ;

    fun getIndex() = entries.indexOf(this)

    fun getNext() = if (this == entries.last()) this else entries[entries.indexOf(this) + 1]

    fun getPrevious() = if (this == entries.first()) this else entries[entries.indexOf(this) - 1]
}
