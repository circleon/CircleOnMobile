package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.Role
import java.io.Serializable
import java.time.LocalTime

data class CircleDetailModel(
    val id: Int,
    val name: String,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val category: Category,
    val comment: String,
    val memberCount: Int,
    val introImgUrl: String?,
    val introduction: String,
    val recruitmentStartDate: LocalTime?,
    val recruitmentEndDate: LocalTime?,
    val memberRole: Role,
    val memberId: Int,
) : Serializable {
    fun isMember() = !memberRole.isSame(Role.NONE)
}
