package com.developeek.circleon.data.dto.auth

import com.google.gson.annotations.SerializedName

data class PolicyId(
    @SerializedName("policyId") val data: String,
)

data class PublicId(
    @SerializedName("publicId") val data: String,
)
