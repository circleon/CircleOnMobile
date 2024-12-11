package com.developeek.circleon.data.dto.login

data class Token(
    val accessToken: String,
    val refreshToken: String,
)

data class AccessToken(
    val accessToken: String,
)

data class RefreshToken(
    val refreshToken: String,
)
