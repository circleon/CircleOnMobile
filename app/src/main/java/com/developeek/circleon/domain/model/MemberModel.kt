package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.Role
import java.io.Serializable

data class MemberModels(private val models: List<MemberModel>) : Serializable {
    fun get() = models

    fun size() = models.size

    fun isEmpty() = models.isEmpty()

    companion object {
        fun empty() = MemberModels(emptyList())
    }
}

data class MemberModel(
    val id: Int,
    val name: String,
    val status: MembershipStatus,
    val role: Role,
    val profileImgUrl: String?,
) : Serializable {
    fun isSame(target: MemberModel) = this.id == target.id

    fun areContentsSame(target: MemberModel) = this == target
}
