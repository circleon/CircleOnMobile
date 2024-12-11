package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.utils.Const
import java.time.LocalTime
import java.util.EmptyStackException
import java.util.Stack

data class PostModel(
    val id: Int,
    val isPinned: Boolean,
    val postImgUrl: String?,
    val content: String,
    val createdAt: LocalTime,
    val updatedAt: LocalTime,
    val commentCount: Int,
    val author: AuthorModel,
) {
    companion object {
        fun emptyInstance() =
            PostModel(
                0,
                false,
                null,
                Const.EMPTY_TEXT,
                LocalTime.of(0, 0),
                LocalTime.of(0, 0),
                0,
                AuthorModel.emptyInstance(),
            )
    }
}

data class AuthorModel(
    val id: Int,
    val name: String,
    val profileUrl: String?,
) {
    companion object {
        fun emptyInstance() =
            AuthorModel(
                0,
                Const.EMPTY_TEXT,
                null,
            )
    }
}

data class PostModels(private val data: List<PostModel>) {
    private val models = Stack<PostModel>()
    private var isLastPage = false

    init {
        for (c in data) {
            models.push(c)
        }
    }

    fun get() = models

    fun get(index: Int) = models[index] ?: throw EmptyStackException()

    fun size() = models.size

    fun add(model: PostModel): PostModels {
        val tmp = Stack<PostModel>()

        tmp.addAll(this.models)
        tmp.add(model)

        return PostModels(tmp)
    }

    fun addAll(models: PostModels): PostModels {
        val tmp = Stack<PostModel>()

        tmp.addAll(this.models)
        tmp.addAll(models.get())

        return PostModels(tmp)
    }

    fun setAsLast() {
        isLastPage = true
    }

    fun isLastPage() = isLastPage

    companion object {
        fun emptyInstance() = PostModels(listOf())
    }
}
