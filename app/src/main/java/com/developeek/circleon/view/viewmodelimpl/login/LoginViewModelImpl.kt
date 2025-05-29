package com.developeek.circleon.view.viewmodelimpl.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.viewmodel.login.LoginViewModel
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
    constructor(private val repository: LoginRepository) : LoginViewModel, ViewModel() {
        private val _event = MutableSharedFlow<LoginEvent>()
        override val event: SharedFlow<LoginEvent> = _event

        private var loginJob: Job? = null
        private val dispatcher = Dispatchers.IO

        override fun login(
            email: String,
            password: String,
        ) {
            loginJob?.let {
                if (!it.isCompleted) return
            }

            viewModelScope.launch {
                if (!checkEmailFormat(email)) return@launch

                _event.emit(LoginEvent.ShowLoadingView)

                loginJob =
                    launch {
                        when (val result = userLogin(email, password)) {
                            is Success -> _event.emit(LoginEvent.SendToHomeScreen)
                            is Error -> _event.emit(LoginEvent.ShowDialog(result.message()))
                        }
                    }
            }
        }

        private suspend fun userLogin(
            email: String,
            password: String,
        ) = withContext(dispatcher) {
            repository.login(email, password)
        }

        private suspend fun checkEmailFormat(email: String): Boolean {
            val result = Validator.checkEmailAsId(email)

            return if (result is Invalid) {
                _event.emit(LoginEvent.ShowDialog(result.message()))
                false
            } else {
                true
            }
        }
    }

sealed class LoginEvent {
    data object ShowLoadingView : LoginEvent()

    data object SendToHomeScreen : LoginEvent()

    data class ShowDialog(val text: String) : LoginEvent()
}
