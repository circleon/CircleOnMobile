package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.state.UiState

interface LoginViewModel {
    val state: LiveData<UiState>
    var error: String

    fun login(
        email: String,
        password: String,
    )
}
