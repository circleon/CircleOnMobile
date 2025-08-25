package com.developeek.circleon.view.viewmodelimpl.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.UserRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.UserModel
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
        private val repository: UserRepository,
    ) : MyPageViewModel, ViewModel() {
        override val user: UserModel
            get() = (repository.getUser() as Success).data

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _myPageScreenEvent = MutableSharedFlow<MyPageScreenEvent>()
        override val myPageScreenEvent: SharedFlow<MyPageScreenEvent> = _myPageScreenEvent

        private var userRequestJob: Job? = null
        private val dispatcher = Dispatchers.IO

        override fun setUserProfileImage(image: File?) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = putUserProfileImage(image)
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> whenPutUserProfileImageSuccess(image)
                        is Error -> whenEditUserProfileImageFail(result)
                    }
                }
        }

        private suspend fun putUserProfileImage(profileImage: File?) =
            withContext(dispatcher) {
                repository.putUserProfileImage(profileImage)
            }

        private suspend fun whenPutUserProfileImageSuccess(image: File?) {
            _event.emit(Event.ShowToast(MESSAGE_SUCCESS_SET_USER_PROFILE_IMAGE))

            updateUser()
            _myPageScreenEvent.emit(MyPageScreenEvent.SetUserProfileImage)
        }

        private suspend fun whenEditUserProfileImageFail(result: Error<Unit>) {
            if (result.isAuthenticationError()) {
                _event.emit(Event.ShowToast(result.message()))
                _event.emit(Event.SendToLoginScreen)
                return
            }

            _event.emit(Event.ShowDialog(result.message()))
        }

        private suspend fun updateUser() =
            withContext(dispatcher) {
                repository.putUser()
            }

        override fun removeUserProfileImage() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = deleteUserProfileImage()
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> whenDeleteUserProfileImageSuccess()
                        is Error -> whenEditUserProfileImageFail(result)
                    }
                }
        }

        private suspend fun whenDeleteUserProfileImageSuccess() {
            _event.emit(Event.ShowToast(MESSAGE_SUCCESS_DELETE_USER_PROFILE_IMAGE))

            updateUser()
            _myPageScreenEvent.emit(MyPageScreenEvent.RemoveUserProfileImage)
        }

        private suspend fun deleteUserProfileImage() =
            withContext(dispatcher) {
                repository.deleteUserProfileImage()
            }

        override fun logout() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = userLogout()
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> showToastAndSendToLoginScreen(MESSAGE_SUCCESS_LOGOUT)
                        is Error -> _event.emit(Event.ShowDialog(result.message()))
                    }
                }
        }

        private suspend fun userLogout() =
            withContext(dispatcher) {
                repository.logout()
            }

        private suspend fun showToastAndSendToLoginScreen(message: String) {
            _event.emit(Event.ShowToast(message))
            _event.emit(Event.SendToLoginScreen)
        }

        override fun resign() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)
                    val result = userResign()
                    _event.emit(Event.EndProcessing)

                    when (result) {
                        is Success -> showToastAndSendToLoginScreen(MESSAGE_SUCCESS_RESIGN)
                        is Error -> _event.emit(Event.ShowDialog(result.message()))
                    }
                }
        }

        private suspend fun userResign() =
            withContext(dispatcher) {
                repository.resign()
            }

        companion object {
            private const val MESSAGE_SUCCESS_SET_USER_PROFILE_IMAGE = "프로필 이미지가 수정되었어요"
            private const val MESSAGE_SUCCESS_DELETE_USER_PROFILE_IMAGE = "프로필 이미지가 삭제되었어요"
            private const val MESSAGE_SUCCESS_LOGOUT = "로그아웃 완료! 다음에 또 만나요 "
            private const val MESSAGE_SUCCESS_RESIGN = "회원탈퇴가 완료되었어요"
        }
    }

sealed class MyPageScreenEvent {
    data object SetUserProfileImage : MyPageScreenEvent()

    data object RemoveUserProfileImage : MyPageScreenEvent()
}
