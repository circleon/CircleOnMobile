package com.developeek.circleon.data.dto.auth

data class LoginResult(
    val user: User,
    val token: Token,
)
