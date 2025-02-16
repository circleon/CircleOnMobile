package com.developeek.circleon.data.dto.home

import android.util.TimeFormatException
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.utils.Utils
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class CircleDetail(
    @SerializedName("circleId") val id: Int,
    @SerializedName("circleName") val name: String,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val category: String,
    @SerializedName("summary") val singleLineIntroduction: String,
    val memberCount: Int,
    val introImgUrl: String?,
    val introduction: String,
    val recruitmentStartDate: String?,
    val recruitmentEndDate: String?,
    @SerializedName("circleRole") val memberRole: String?,
    val memberId: Int?,
) {
    fun toCircleDetailModel() =
        CircleDetailModel(
            id,
            name,
            Role.findOrDefault(memberRole),
            memberId(memberId),
            Utils.getCircleImageUrlOrNull(profileImgUrl),
            Utils.getCircleImageUrlOrNull(thumbnailUrl),
            Category.findOrDefault(category),
            singleLineIntroduction,
            memberCount,
            Utils.getCircleImageUrlOrNull(introImgUrl),
            introduction,
            localDateTime(recruitmentStartDate),
            localDateTime(recruitmentEndDate),
        )

    private fun localDateTime(dateTime: String?): LocalDateTime? {
        dateTime ?: return null

        return try {
            LocalDateTime.parse(dateTime)
        } catch (e: TimeFormatException) {
            null
        }
    }

    private fun memberId(id: Int?) = id ?: 0
}

data class RequestBodyEditCircleDetail(
    @SerializedName("circleName") val name: String,
    @SerializedName("summary") val singleLineIntroduction: String,
    val introduction: String,
    val recruitmentStartDate: String?,
    val recruitmentEndDate: String?,
    @SerializedName("categoryType") val category: String,
)
