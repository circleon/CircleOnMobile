package com.developeek.circleon.data.entity

import com.google.gson.annotations.SerializedName

data class SignUpEntity(
    val email: String,
    @SerializedName("username") val name: String,
    val password: String,
)
