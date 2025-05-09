package com.developeek.circleon.data.dto.home

import android.util.TimeFormatException
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.OfficialStatus
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.utils.Utils
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class CircleDetail(
    @SerializedName("circleId") val id: Int,
    @SerializedName("circleName") val name: String,
    val category: String,
    val officialStatus: String,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    @SerializedName("summary") val singleLineIntroduction: String,
    val memberCount: Int,
    val introImgUrl: String?,
    val introduction: String?,
    val recruitmentStartDate: String?,
    val recruitmentEndDate: String?,
    val recruiting: Boolean,
    @SerializedName("circleRole") val role: String?,
    val memberId: Int?,
    val membershipStatus: String?,
) {
    fun toCircleDetailModel(circleMembers: MemberModels) =
        CircleDetailModel(
            id,
            name,
            Category.findOrDefault(category),
            OfficialStatus.find(officialStatus),
            Role.findOrDefault(role),
            memberId(memberId),
            MembershipStatus.findOrDefault(membershipStatus),
            Utils.getCircleImageUrlOrNull(profileImgUrl),
            Utils.getCircleImageUrlOrNull(thumbnailUrl),
            singleLineIntroduction,
            memberCount,
            circleMembers,
            Utils.getCircleImageUrlOrNull(introImgUrl),
            introduction,
            localDateTime(recruitmentStartDate),
            localDateTime(recruitmentEndDate),
            recruiting,
        )

    private fun localDateTime(dateTime: String?): LocalDateTime? {
        dateTime ?: return null

        return try {
            LocalDateTime.parse(dateTime)
        } catch (e: TimeFormatException) {
            null
        }
    }

    // TODO: 동아리 가입 상태와 상관없이 Member DTO 적용중인데, 막상 memberId 는 가입해야만 값이 넘어옴.
    // 가입 신청 같은 경우에 memberModel 로 다뤄야돼서 수정이 필요할듯
    private fun memberId(id: Int?) = id ?: 0
}
