package com.developeek.circleon.data.entity.login

data class Email(
    val email: String,
)

data class EmailAuthentication(
    val email: String,
    val code: String,
)
