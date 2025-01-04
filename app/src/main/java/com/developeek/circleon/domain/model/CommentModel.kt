package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.utils.Const
import java.time.LocalDateTime
import java.util.EmptyStackException
import java.util.Stack

data class CommentModels(private val data: List<CommentModel>) {
    private val models = Stack<CommentModel>()
    private var isLastPage = false

    init {
        for (c in data) {
            models.push(c)
        }
    }

    fun get() = models

    fun get(index: Int) = models[index] ?: throw EmptyStackException()

    fun size() = models.size

    fun isEmpty() = models.isEmpty()

    fun add(model: CommentModel): CommentModels {
        val tmp = Stack<CommentModel>()

        tmp.addAll(this.models)
        tmp.add(model)

        return CommentModels(tmp)
    }

    fun addAll(models: CommentModels): CommentModels {
        val tmp = Stack<CommentModel>()

        tmp.addAll(this.models)
        tmp.addAll(models.get())

        return CommentModels(tmp)
    }

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
