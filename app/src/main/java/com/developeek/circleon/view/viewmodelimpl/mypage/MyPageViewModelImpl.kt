package com.developeek.circleon.view.viewmodelimpl.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.mypage.MyPageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MyPageViewModelImpl
    @Inject
    constructor(
        private val loginRepository: LoginRepository,
        private val circleRepository: CircleRepository,
    ) : MyPageViewModel, ViewModel() {
        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event

        private var userRequestJob: Job? = null
        private val dispatcher = Dispatchers.IO

        override fun setUserProfileImage(image: File?) {
        }

        override fun logout() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)

                    when (val result = userLogout()) {
                        is Success -> whenLogoutSuccess()
                        is Error -> whenLogoutFail(result)
                    }
                }
        }

        private suspend fun userLogout() =
            withContext(dispatcher) {
                loginRepository.logout()
            }

        private suspend fun whenLogoutSuccess() {
            _event.emit(Event.ShowToast(MESSAGE_SUCCESS_LOGOUT))
            _event.emit(Event.SendToLoginScreen)
        }

        private suspend fun whenLogoutFail(result: Error<Unit>) {
            _event.emit(Event.ShowToast(result.message()))
        }

        companion object {
            private const val MESSAGE_SUCCESS_LOGOUT = "로그아웃이 완료됐어요"
        }
    }
