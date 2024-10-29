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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModelImpl
    @Inject
    constructor(private val repository: LoginRepository) : LoginViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        private lateinit var loginJob: Job

        override var error =
            Const.EMPTY_TEXT

        override fun login(
            email: String,
            password: String,
        ) {
            if ((!::loginJob.isInitialized || loginJob.isCompleted) && isEmailFormat(email)) {
                loginJob =
                    viewModelScope.launch {
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

        private fun isEmailFormat(email: String): Boolean {
            val validation = Validator.checkEmailAsId(email)

            return if (validation is Invalid) {
                error = validation.message()
                uiState.postValue(UiState.Error)
                false
            } else {
                true
            }
        }
    }
