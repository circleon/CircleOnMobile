package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Role
import java.time.LocalTime

data class CircleDetailModel(
    val circle: CircleModel,
    val introImgUrl: String?,
    val introduction: String,
    val recruitmentStartDate: LocalTime,
    val recruitmentEndDate: LocalTime,
    val memberRole: Role,
    val memberId: Int,
) {
    fun isMember() = !memberRole.isSame(Role.NONE)
}
