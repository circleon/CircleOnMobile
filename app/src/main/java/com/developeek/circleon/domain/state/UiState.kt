package com.developeek.circleon.domain.state

sealed class UiState {
    object Success : UiState()

    object Loading : UiState()

    object ServiceError : UiState()

    object AuthenticationError : UiState()
}
