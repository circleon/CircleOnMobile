package com.developeek.circleon.data.dto.login

import com.google.gson.annotations.SerializedName

data class SignUp(
    val email: String,
    @SerializedName("username") val name: String,
    val password: String,
)
