package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.utils.Const
import java.io.Serializable
import java.time.LocalDateTime

data class CommentModels(val commentModels: List<CommentModel>) : Models<CommentModel>(commentModels) {
    private var isLastPage = false

    fun add(commentModel: CommentModel) = CommentModels(commentModels + commentModel)

    fun addAll(commentModels: CommentModels) = CommentModels(this.commentModels + commentModels.get())

    fun remove(commentId: Int) = CommentModels(commentModels.filter { it.id != commentId })

    fun setAsLast() {
        isLastPage = true
    }

    fun isLastPage() = isLastPage

    companion object {
        fun emptyInstance() = CommentModels(emptyList())
    }
}

data class CommentModel(
    val commentId: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val author: AuthorModel,
) : BaseModel(commentId), Serializable {
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
