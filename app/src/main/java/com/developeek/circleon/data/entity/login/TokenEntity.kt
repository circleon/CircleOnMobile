package com.developeek.circleon.data.entity.login

data class TokenEntity(
    val userId: Int,
    val accessToken: String,
    val refreshToken: String,
)
