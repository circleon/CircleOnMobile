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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModelImpl
    @Inject
    constructor(private val repository: LoginRepository) : LoginViewModel, ViewModel() {
        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event

        private var loginJob: Job? = null

        override fun login(
            email: String,
            password: String,
        ) {
            loginJob?.let {
                if (!it.isCompleted) return
            }

            viewModelScope.launch {
                checkEmailFormat(email)?.let {
                    _event.emit(Event.ShowErrorDialog(it))
                    return@launch
                }

                _event.emit(Event.ShowLoadingView)

                loginJob =
                    launch {
                        val result = repository.login(email, password)

                        if (result is Success) {
                            _event.emit(Event.SendToHomeScreen)
                        } else {
                            _event.emit(Event.ShowErrorDialog((result as Error).message()))
                        }
                    }
            }
        }

        private fun checkEmailFormat(email: String): String? {
            val validation = Validator.checkEmailAsId(email)

            return if (validation is Invalid) validation.message() else null
        }
    }

sealed class Event {
    data object ShowLoadingView : Event()

    data object SendToHomeScreen : Event()

    data class ShowErrorDialog(val text: String) : Event()
}
