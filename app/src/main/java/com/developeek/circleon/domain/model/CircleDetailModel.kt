package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.Role
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
    val comment: String,
    val memberCount: Int,
    val introImgUrl: String?,
    val introduction: String,
    val recruitmentStartDate: LocalDateTime?,
    val recruitmentEndDate: LocalDateTime?,
) : Serializable {
    fun isMember() = role.isMember()

    fun isExecutive() = role.isExecutive()
}
