package com.developeek.circleon.data.entity.login

import com.google.gson.annotations.SerializedName

data class SignUpEntity(
    val email: String,
    @SerializedName("username") val name: String,
    val password: String,
)
