package com.developeek.circleon.data.dto.home

import android.util.TimeFormatException
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.utils.Utils.circleImageUrl
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class CircleDetail(
    @SerializedName("circleId") val id: Int,
    @SerializedName("circleName") val name: String,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val category: String,
    @SerializedName("summary") val comment: String,
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
            circleImageUrl(profileImgUrl),
            circleImageUrl(thumbnailUrl),
            category(category),
            comment,
            memberCount,
            circleImageUrl(introImgUrl),
            introduction,
            localDateTime(recruitmentStartDate),
            localDateTime(recruitmentEndDate),
            memberRole(memberRole),
            memberId(memberId),
        )

    private fun category(codeName: String): Category {
        val category = Category.findOrNull(codeName)

        return category ?: Category.ETC
    }

    private fun localDateTime(dateTime: String?): LocalDateTime? {
        dateTime ?: return null

        return try {
            LocalDateTime.parse(dateTime)
        } catch (e: TimeFormatException) {
            null
        }
    }

    private fun memberRole(codeName: String?): Role {
        val role = Role.findOrNull(codeName)

        return role ?: Role.NONE
    }

    private fun memberId(id: Int?) = id ?: 0
}
