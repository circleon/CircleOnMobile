package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.developeek.circleon.data.Error
import com.developeek.circleon.data.Success
import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.viewmodel.LoginViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

        private var loginJob: Job? = null

        override var error =
            Const.EMPTY_TEXT

        override fun login(
            email: String,
            password: String,
        ) {
            loginJob?.cancel()

            loginJob =
                CoroutineScope(Dispatchers.IO).launch {
                    userLogin(email, password)
                }
        }

        private suspend fun userLogin(
            email: String,
            password: String,
        ) {
            val result = repository.login(email, password)

            if (result is Success) {
                uiState.postValue(UiState.Success)
            } else {
                if ((result as Error).isTimeOut()) {
                    error = result.message()
                    uiState.postValue(UiState.Timeout)
                }
                if ((result).isRefreshExpired()) {
                    uiState.postValue(UiState.RefreshExpiration)
                }
            }
        }
    }
