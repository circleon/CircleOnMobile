package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.utils.Const
import java.time.LocalDateTime

data class CommentModels(private val models: List<CommentModel>) {
    private var isLastPage = false

    fun get() = models

    fun get(index: Int) = models[index]

    fun last() = models.last()

    fun size() = models.size

    fun isEmpty() = models.isEmpty()

    fun add(commentModel: CommentModel) = CommentModels(models + commentModel)

    fun addAll(commentModels: CommentModels) = CommentModels(models + commentModels.get())

    fun setAsLast() {
        isLastPage = true
    }

    fun isLastPage() = isLastPage

    companion object {
        fun emptyInstance() = CommentModels(listOf())
    }
}

data class CommentModel(
    val id: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val author: AuthorModel,
) {
    fun isSame(commentModel: CommentModel) = this.id == commentModel.id

    fun areContentsSame(commentModel: CommentModel) = this == commentModel

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
