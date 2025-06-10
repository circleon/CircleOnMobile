package com.developeek.circleon.domain.model

import com.developeek.circleon.data.dto.home.RequestBodyEditCircleDetail
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.OfficialStatus
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.utils.Const
import java.time.LocalDateTime

data class CircleDetailModel(
    val circleId: Int,
    val name: String,
    val category: Category,
    val officialStatus: OfficialStatus,
    val role: Role,
    val memberId: Int,
    val membershipStatus: MembershipStatus,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val singleLineIntroduction: String,
    val memberCount: Int,
    val introImgUrl: String?,
    val introduction: String?,
    val recruitmentStartDate: LocalDateTime?,
    val recruitmentEndDate: LocalDateTime?,
    val recruiting: Boolean,
    val members: Models<MemberModel> = Models(),
) : BaseModel(circleId) {
    constructor(circleDetailModel: CircleDetailModel, members: Models<MemberModel>) : this(
        circleDetailModel.id,
        circleDetailModel.name,
        circleDetailModel.category,
        circleDetailModel.officialStatus,
        circleDetailModel.role,
        circleDetailModel.memberId,
        circleDetailModel.membershipStatus,
        circleDetailModel.profileImgUrl,
        circleDetailModel.thumbnailUrl,
        circleDetailModel.singleLineIntroduction,
        circleDetailModel.memberCount,
        circleDetailModel.introImgUrl,
        circleDetailModel.introduction,
        circleDetailModel.recruitmentStartDate,
        circleDetailModel.recruitmentEndDate,
        circleDetailModel.recruiting,
        members,
    )

    override fun areContentsSame(target: BaseModel): Boolean {
        if (target !is CircleDetailModel) return false

        return this == target
    }

    fun isOfficial() = officialStatus.isOfficial()

    fun isUserJoined() = role.isMember()

    fun isUserExecutive() = role.isExecutive()

    fun isUserPresident() = role.isPresident()

    fun fold(
        id: Int = this.id,
        name: String = this.name,
        category: Category = this.category,
        officialStatus: OfficialStatus = this.officialStatus,
        role: Role = this.role,
        memberId: Int = this.memberId,
        membershipStatus: MembershipStatus = this.membershipStatus,
        profileImgUrl: String? = this.profileImgUrl,
        thumbnailUrl: String? = this.thumbnailUrl,
        singleLineIntroduction: String = this.singleLineIntroduction,
        memberCount: Int = this.memberCount,
        introImgUrl: String? = this.introImgUrl,
        introduction: String? = this.introduction,
        recruitmentStartDate: LocalDateTime? = this.recruitmentStartDate,
        recruitmentEndDate: LocalDateTime? = this.recruitmentEndDate,
        recruiting: Boolean = this.recruiting,
        members: Models<MemberModel> = this.members,
    ) = CircleDetailModel(
        id,
        name,
        category,
        officialStatus,
        role,
        memberId,
        membershipStatus,
        profileImgUrl,
        thumbnailUrl,
        singleLineIntroduction,
        memberCount,
        introImgUrl,
        introduction,
        recruitmentStartDate,
        recruitmentEndDate,
        recruiting,
        members,
    )

    fun toRequestBodyForEdit() =
        RequestBodyEditCircleDetail(
            this.name,
            this.category.codeName,
            this.singleLineIntroduction,
            this.introduction,
            this.recruitmentStartDate?.toString(),
            this.recruitmentEndDate?.toString(),
            this.recruiting,
        )

    companion object {
        fun empty() =
            CircleDetailModel(
                0,
                Const.EMPTY_TEXT,
                Category.ETC,
                OfficialStatus.UNOFFICIAL,
                Role.NONE_MEMBER,
                0,
                MembershipStatus.NOT_JOINED,
                null,
                null,
                Const.EMPTY_TEXT,
                0,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
            )
    }
}
