package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.UserRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModelImpl
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) : MainViewModel, ViewModel() {
        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event

        override fun checkUser() {
            viewModelScope.launch {
                val result = userRepository.getUser()

                if (result is Error) {
                    _event.emit(Event.SendToLoginScreen)
                }
            }
        }
    }
