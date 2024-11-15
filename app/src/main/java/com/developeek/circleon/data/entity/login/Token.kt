package com.developeek.circleon.data.entity.login

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
