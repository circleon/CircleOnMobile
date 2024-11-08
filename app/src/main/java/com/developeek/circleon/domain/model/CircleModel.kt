package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import java.util.EmptyStackException
import java.util.Stack

data class CircleModel(
    val id: Int,
    val name: String,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val category: Category,
    val member: Int,
)

data class CircleModels(private val data: List<CircleModel>) {
    private val models = Stack<CircleModel>()

    init {
        for (c in data) {
            models.push(c)
        }
    }

    fun get() = models

    fun get(index: Int) = models[index] ?: throw EmptyStackException()

    fun size() = models.size

    fun add(models: CircleModels): CircleModels {
        val tmp = Stack<CircleModel>()

        tmp.addAll(this.models)
        tmp.addAll(models.get())

        return CircleModels(tmp)
    }

    companion object {
        fun emptyInstance() = CircleModels(listOf())
    }
}
