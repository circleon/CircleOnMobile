package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Role
import java.io.Serializable

data class MemberModels(private val models: List<MemberModel>) : Serializable {
    fun size() = models.size

    companion object {
        fun empty() = MemberModels(emptyList())
    }
}

data class MemberModel(
    val id: Int,
    val name: String,
    val role: Role,
    val profileImgUrl: String?,
) : Serializable
