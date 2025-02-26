package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.utils.Const
import java.io.Serializable
import java.time.LocalDateTime

data class PostModels(private val models: List<PostModel>) {
    private var isLastPage = false

    fun get() = models

    fun get(index: Int) = models[index]

    fun size() = models.size

    fun isEmpty() = models.isEmpty()

    fun add(postModel: PostModel) = PostModels(models + postModel)

    fun addAll(postModels: PostModels) = PostModels(models + postModels.get())

    fun setAsLast() {
        isLastPage = true
    }

    fun isLastPage() = isLastPage

    companion object {
        fun emptyInstance() = PostModels(emptyList())
    }
}

data class PostModel(
    override val id: Int,
    val type: PostType,
    val isPinned: Boolean,
    val imgUrl: String?,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val commentCount: Int,
    val author: AuthorModel,
) : Serializable, Identifiable {
    override fun isSame(target: Identifiable) = this.id == target.id

    override fun areContentsSame(target: Identifiable) = this == target

    fun isNotice() = this.type.isNotice()

    fun isPost() = !isNotice()

    fun areContentsSame(postModel: PostModel) = this == postModel

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
