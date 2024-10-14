package com.developeek.circleon.domain.state

sealed class UiState {
    object Success : UiState()

    object Loading : UiState()

    object Timeout : UiState()

    object Error : UiState()

    object RefreshExpiration : UiState()
}
