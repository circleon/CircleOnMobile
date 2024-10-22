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
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.viewmodel.LoginViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModelImpl
    @Inject
    constructor(private val repository: LoginRepository) : LoginViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        private var loginJob: Job? = null

        override var error =
            Const.EMPTY_TEXT

        override fun login(
            email: String,
            password: String,
        ) {
            loginJob?.cancel()

            loginJob =
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        userLogin(email, password)
                    }
                }
        }

        private suspend fun userLogin(
            email: String,
            password: String,
        ) {
            val validation = Validator.checkEmailAsId(email)

            if (validation is Invalid) {
                error = validation.message()
                uiState.postValue(UiState.Error)
            } else {
                val result = repository.login(email, password)

                if (result is Success) {
                    uiState.postValue(UiState.Success)
                } else {
                    error = (result as Error).message()
                    uiState.postValue(UiState.Error)
                }
            }
        }
    }
