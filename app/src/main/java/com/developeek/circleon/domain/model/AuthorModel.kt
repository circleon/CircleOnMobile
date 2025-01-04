package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.utils.Const
import java.io.Serializable

data class AuthorModel(
    val id: Int,
    val name: String,
    val profileUrl: String?,
) : Serializable {
    companion object {
        fun emptyInstance() =
            AuthorModel(
                0,
                Const.EMPTY_TEXT,
                null,
            )
    }
}
