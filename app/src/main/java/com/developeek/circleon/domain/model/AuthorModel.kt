package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.utils.Const
import java.io.Serializable

data class AuthorModel(
    val id: Int,
    val name: String,
    val profileUrl: String?,
) : Serializable {
    fun isSame(authorModel: AuthorModel) = this.id == authorModel.id

    fun areContentsSame(authorModel: AuthorModel) = this == authorModel

    companion object {
        fun emptyInstance() =
            AuthorModel(
                0,
                Const.EMPTY_TEXT,
                null,
            )
    }
}
