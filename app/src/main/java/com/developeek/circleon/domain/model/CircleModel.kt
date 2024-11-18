package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.utils.Const
import java.io.Serializable
import java.util.EmptyStackException
import java.util.Stack

data class CircleModel(
    val id: Int,
    val name: String,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val category: Category,
    val member: Int,
) : Serializable {
    companion object {
        fun emptyInstance() =
            CircleModel(
                0,
                Const.EMPTY_TEXT,
                null,
                null,
                Category.ETC,
                0,
            )
    }
}

data class CircleModels(private val data: List<CircleModel>) {
    private val models = Stack<CircleModel>()
    private var isLastPage = false

    init {
        for (c in data) {
            models.push(c)
        }
    }

    fun get() = models

    fun get(index: Int) = models[index] ?: throw EmptyStackException()

    fun size() = models.size

    fun add(model: CircleModel): CircleModels {
        val tmp = Stack<CircleModel>()

        tmp.addAll(this.models)
        tmp.add(model)

        return CircleModels(tmp)
    }

    fun addAll(models: CircleModels): CircleModels {
        val tmp = Stack<CircleModel>()

        tmp.addAll(this.models)
        tmp.addAll(models.get())

        return CircleModels(tmp)
    }

    fun setAsLast() {
        isLastPage = true
    }

    fun isLastPage() = isLastPage

    companion object {
        fun emptyInstance() = CircleModels(listOf())
    }
}
