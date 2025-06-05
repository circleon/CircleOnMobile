package com.developeek.circleon.view.viewmodelimpl.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.AuthRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.auth.LoginViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModelImpl
    @Inject
    constructor(private val repository: AuthRepository) : LoginViewModel, ViewModel() {
        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _loginScreenEvent = MutableSharedFlow<LoginScreenEvent>()
        override val loginScreenEvent: SharedFlow<LoginScreenEvent> = _loginScreenEvent

        private var loginJob: Job? = null
        private val dispatcher = Dispatchers.IO

        override fun login(
            email: String,
            password: String,
        ) {
            loginJob?.let {
                if (!it.isCompleted) return
            }

            loginJob =
                viewModelScope.launch {
                    if (!checkEmailFormat(email)) return@launch
                    _event.emit(Event.ShowProcessing)

                    when (val result = userLogin(email, password)) {
                        is Success -> whenLoginSuccess()
                        is Error -> _event.emit(Event.ShowDialog(result.message()))
                    }
                }
        }

        private suspend fun userLogin(
            email: String,
            password: String,
        ) = withContext(dispatcher) {
            repository.login(email, password)
        }

        private suspend fun whenLoginSuccess() {
            _event.emit(Event.ShowToast(MESSAGE_LOGIN_SUCCESS))
            _loginScreenEvent.emit(LoginScreenEvent.SendToHomeScreen)
        }

        private suspend fun checkEmailFormat(email: String): Boolean {
            val result = Validator.checkEmailAsId(email)

            return if (result is Invalid) {
                _event.emit(Event.ShowDialog(result.message()))
                false
            } else {
                true
            }
        }

        companion object {
            private const val MESSAGE_LOGIN_SUCCESS = "환영합니다"
        }
    }

sealed class LoginScreenEvent {
    data object SendToHomeScreen : LoginScreenEvent()
}
