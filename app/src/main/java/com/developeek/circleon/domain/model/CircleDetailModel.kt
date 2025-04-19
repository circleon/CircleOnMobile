package com.developeek.circleon.domain.model

import com.developeek.circleon.data.dto.home.RequestBodyEditCircleDetail
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.utils.Const
import java.io.Serializable
import java.time.LocalDateTime

data class CircleDetailModel(
    val id: Int,
    val name: String,
    val role: Role,
    val memberId: Int,
    val membershipStatus: MembershipStatus,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val category: Category,
    val singleLineIntroduction: String,
    val memberCount: Int,
    val members: MemberModels,
    val introImgUrl: String?,
    val introduction: String?,
    val recruitmentStartDate: LocalDateTime?,
    val recruitmentEndDate: LocalDateTime?,
) : Serializable {
    fun isUserJoined() = role.isMember()

    fun isUserExecutive() = role.isExecutive()

    fun isUserPresident() = role.isPresident()

    fun fold(
        id: Int = this.id,
        name: String = this.name,
        role: Role = this.role,
        memberId: Int = this.memberId,
        membershipStatus: MembershipStatus = this.membershipStatus,
        profileImgUrl: String? = this.profileImgUrl,
        thumbnailUrl: String? = this.thumbnailUrl,
        category: Category = this.category,
        singleLineIntroduction: String = this.singleLineIntroduction,
        memberCount: Int = this.memberCount,
        members: MemberModels = this.members,
        introImgUrl: String? = this.introImgUrl,
        introduction: String? = this.introduction,
        recruitmentStartDate: LocalDateTime? = this.recruitmentStartDate,
        recruitmentEndDate: LocalDateTime? = this.recruitmentEndDate,
    ) = CircleDetailModel(
        id,
        name,
        role,
        memberId,
        membershipStatus,
        profileImgUrl,
        thumbnailUrl,
        category,
        singleLineIntroduction,
        memberCount,
        members,
        introImgUrl,
        introduction,
        recruitmentStartDate,
        recruitmentEndDate,
    )

    fun toRequestBodyForEdit() =
        RequestBodyEditCircleDetail(
            this.name,
            this.singleLineIntroduction,
            this.introduction,
            this.recruitmentStartDate?.toString(),
            this.recruitmentEndDate?.toString(),
            this.category.codeName(),
        )

    companion object {
        fun empty() =
            CircleDetailModel(
                0,
                Const.EMPTY_TEXT,
                Role.NONE_MEMBER,
                0,
                MembershipStatus.NOT_JOINED,
                null,
                null,
                Category.ETC,
                Const.EMPTY_TEXT,
                0,
                MemberModels.empty(),
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
            )
    }
}
