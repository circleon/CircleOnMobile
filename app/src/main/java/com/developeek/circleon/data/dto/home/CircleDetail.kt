package com.developeek.circleon.data.dto.home

import com.developeek.circleon.BuildConfig
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.google.gson.annotations.SerializedName
import java.time.LocalTime

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
    val recruitmentStartDate: LocalTime,
    val recruitmentEndDate: LocalTime,
    @SerializedName("circleRole") val memberRole: String?,
    val memberId: Int?,
) {
    fun toCircleDetailModel() =
        CircleDetailModel(
            id,
            name,
            imageUrl(profileImgUrl),
            imageUrl(thumbnailUrl),
            category(category),
            comment,
            memberCount,
            imageUrl(introImgUrl),
            introduction,
            recruitmentStartDate,
            recruitmentEndDate,
            memberRole(memberRole),
            memberId(memberId),
        )

    private fun category(codeName: String): Category {
        val category = Category.findOrNull(codeName)

        return category ?: Category.ETC
    }

    private fun imageUrl(url: String?): String? {
        url ?: return null

        return BuildConfig.SERVICE_API_URL + IMAGE_PATH + url
    }

    private fun memberRole(codeName: String?): Role {
        val role = Role.findOrNull(codeName)

        return role ?: Role.NONE
    }

    private fun memberId(id: Int?) = id ?: 0

    companion object {
        private const val IMAGE_PATH = "circles/images/"
    }
}
