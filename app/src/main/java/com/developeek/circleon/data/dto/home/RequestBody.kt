package com.developeek.circleon.data.dto.home

import com.google.gson.annotations.SerializedName

data class RequestBodyReport(val reason: String)

data class RequestBodyEditCircleDetail(
    @SerializedName("circleName") val name: String,
    @SerializedName("categoryType") val category: String,
    @SerializedName("summary") val singleLineIntroduction: String,
    val introduction: String?,
    val recruitmentStartDate: String?,
    val recruitmentEndDate: String?,
    val recruiting: Boolean,
)

data class RequestBodyEditComment(
    val content: String,
)

data class RequestBodyEditMemberRole(
    @SerializedName("circleRole") val role: String,
)

data class RequestBodyEditMemberStatus(
    @SerializedName("membershipStatus") val status: String,
)

data class RequestResponseBodyCircleJoin(
    @SerializedName("joinMessage") val message: String,
)

data class RequestResponseBodyCircleLeave(
    @SerializedName("leaveMessage") val message: String,
)

data class RequestBodyEditPost(
    val postType: String,
    val content: String,
)
