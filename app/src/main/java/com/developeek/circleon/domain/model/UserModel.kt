package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.University

data class UserModel(
    val userId: Int,
    val name: String,
    val univ: University,
    val profileImage: String?,
) : BaseModel(userId) {
    override fun areContentsSame(target: BaseModel): Boolean {
        if (target !is UserModel) return false

        return this == target
    }
}
