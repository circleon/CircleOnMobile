package com.developeek.circleon.data.dto.auth

import com.google.gson.annotations.SerializedName

data class LogoutRequestBody(val refreshToken: String)

data class SignUpRequestBody(
    val email: String,
    @SerializedName("username") val name: String,
    val password: String,
    val serviceTerms: Boolean,
    val privacyPolicies: Boolean,
    val communityRules: Boolean,
)

data class NewPasswordRequestBody(
    val publicId: String,
    @SerializedName("newPassword") val password: String,
)

data class EmailCodeRequestBody(
    val email: String,
)

data class EmailAuthenticationRequestBody(
    val email: String,
    val code: String,
)

data class NewPasswordEmailAuthenticationRequestBody(
    val policyId: String,
    val code: String,
)
