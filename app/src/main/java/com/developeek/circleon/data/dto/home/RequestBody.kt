package com.developeek.circleon.data.dto.home

import com.google.gson.annotations.SerializedName

data class RequestBodyReport(val reason: String)

data class RequestBodyEditCircleDetail(
    @SerializedName("circleName") val name: String,
    @SerializedName("summary") val singleLineIntroduction: String,
    val introduction: String?,
    val recruitmentStartDate: String?,
    val recruitmentEndDate: String?,
    @SerializedName("categoryType") val category: String,
)
