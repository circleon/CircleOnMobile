package com.developeek.circleon.data.dto.login

data class Email(
    val email: String,
)

data class EmailAuthentication(
    val email: String,
    val code: String,
)
