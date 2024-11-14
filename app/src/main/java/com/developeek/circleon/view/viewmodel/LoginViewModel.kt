package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.UserModel
import com.developeek.circleon.domain.state.UiState

interface LoginViewModel {
    val state: LiveData<UiState>
    val user: UserModel
    var error: String

    fun login(
        email: String,
        password: String,
    )
}
