package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.utils.Const
import java.io.Serializable
import java.time.LocalDateTime

data class PostModel(
    val postId: Int,
    val type: PostType,
    val isPinned: Boolean,
    val imgUrl: String?,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val commentCount: Int,
    val author: AuthorModel,
) : BaseModel(postId), Serializable {
    fun isNotice() = this.type == PostType.NOTICE

    fun isPost() = !isNotice()

    companion object {
        fun emptyInstance() =
            PostModel(
                0,
                PostType.POST,
                false,
                null,
                Const.EMPTY_TEXT,
                LocalDateTime.of(1, 1, 1, 1, 1),
                LocalDateTime.of(1, 1, 1, 1, 1),
                0,
                AuthorModel.emptyInstance(),
            )
    }
}
