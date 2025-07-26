package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.utils.Const

data class AuthorModel(
    val authorId: Int,
    val name: String,
    val profileImgUrl: String?,
) : BaseModel(authorId) {
    override fun areContentsSame(target: BaseModel): Boolean {
        if (target !is AuthorModel) return false

        return this == target
    }

    companion object {
        fun emptyInstance() =
            AuthorModel(
                0,
                Const.EMPTY_TEXT,
                null,
            )
    }
}
