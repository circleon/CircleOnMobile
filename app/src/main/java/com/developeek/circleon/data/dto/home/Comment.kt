package com.developeek.circleon.data.dto.home

import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.utils.Utils
import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("commentId") val id: Int,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val author: Author,
) {
    fun toCommentModel() =
        CommentModel(
            id,
            content,
            Utils.getLocalDateTimeOrDefault(createdAt),
            Utils.getLocalDateTimeOrDefault(updatedAt),
            author.toAuthorModel(),
        )
}

data class CommentContent(
    val content: String,
)
