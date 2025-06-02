package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.utils.Const
import java.io.Serializable
import java.time.LocalDateTime

data class CommentModel(
    val commentId: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val author: AuthorModel,
) : BaseModel(commentId), Serializable {
    override fun areContentsSame(target: BaseModel): Boolean {
        if (target !is CommentModel) return false

        return this == target
    }

    companion object {
        fun emptyInstance() =
            CommentModel(
                0,
                Const.EMPTY_TEXT,
                LocalDateTime.of(1, 1, 1, 1, 1),
                LocalDateTime.of(1, 1, 1, 1, 1),
                AuthorModel.emptyInstance(),
            )
    }
}
