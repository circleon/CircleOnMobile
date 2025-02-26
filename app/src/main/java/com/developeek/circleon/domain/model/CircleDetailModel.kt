package com.developeek.circleon.domain.model

import com.developeek.circleon.data.dto.home.RequestBodyEditCircleDetail
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.utils.Const
import java.io.Serializable
import java.time.LocalDateTime

data class CircleDetailModel(
    val id: Int,
    val name: String,
    val role: Role,
    val memberId: Int,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val category: Category,
    val singleLineIntroduction: String,
    val memberCount: Int,
    val members: MemberModels,
    val introImgUrl: String?,
    val introduction: String,
    val recruitmentStartDate: LocalDateTime?,
    val recruitmentEndDate: LocalDateTime?,
) : Serializable {
    fun isMember() = role.isMember()

    fun isExecutive() = role.isExecutive()

    fun fold(
        id: Int = this.id,
        name: String = this.name,
        role: Role = this.role,
        memberId: Int = this.memberId,
        profileImgUrl: String? = this.profileImgUrl,
        thumbnailUrl: String? = this.thumbnailUrl,
        category: Category = this.category,
        singleLineIntroduction: String = this.singleLineIntroduction,
        memberCount: Int = this.memberCount,
        members: MemberModels = this.members,
        introImgUrl: String? = this.introImgUrl,
        introduction: String = this.introduction,
        recruitmentStartDate: LocalDateTime? = this.recruitmentStartDate,
        recruitmentEndDate: LocalDateTime? = this.recruitmentEndDate,
    ) = CircleDetailModel(
        id,
        name,
        role,
        memberId,
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
            this.recruitmentStartDate.toString(),
            this.recruitmentEndDate.toString(),
            this.category.codeName(),
        )

    companion object {
        fun empty() =
            CircleDetailModel(
                0,
                Const.EMPTY_TEXT,
                Role.NONE_MEMBER,
                0,
                null,
                null,
                Category.ETC,
                Const.EMPTY_TEXT,
                0,
                MemberModels.empty(),
                null,
                Const.EMPTY_TEXT,
                null,
                null,
            )
    }
}
