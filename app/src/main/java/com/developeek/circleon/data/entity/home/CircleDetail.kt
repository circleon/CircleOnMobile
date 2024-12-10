package com.developeek.circleon.data.entity.home

import com.developeek.circleon.BuildConfig
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.google.gson.annotations.SerializedName
import java.time.LocalTime

data class CircleDetail(
    val circle: Circle,
    val introImgUrl: String?,
    val introduction: String,
    val recruitmentStartDate: LocalTime,
    val recruitmentEndDate: LocalTime,
    @SerializedName("circleRole") val memberRole: String?,
    val memberId: Int?,
) {
    fun toCircleDetailModel() =
        CircleDetailModel(
            circle.toCircleModel(),
            imageUrl(introImgUrl),
            introduction,
            recruitmentStartDate,
            recruitmentEndDate,
            memberRole(memberRole),
            memberId(memberId),
        )

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
