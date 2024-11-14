package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import java.io.Serializable
import java.util.EmptyStackException
import java.util.Stack
import kotlin.math.absoluteValue

data class CircleSummaryModel(
    val id: Int,
    val name: String,
    val category: Category,
) : Serializable

data class CircleSummaryModels(private val data: List<CircleSummaryModel>) {
    private val models = Stack<CircleSummaryModel>()

    init {
        for (c in data) {
            models.push(c)
        }
    }

    fun get() = models

    fun get(index: Int) = models[index] ?: throw EmptyStackException()

    fun isEmpty() = models.isEmpty()

    fun find(keyword: String) =
        CircleSummaryModels(
            models.filter { it.name.contains(keyword) }
                .sortedWith(
                    compareBy<CircleSummaryModel> { it.name.compareTo(keyword).absoluteValue }
                        .thenBy { it.name }
                        .thenBy { it.category },
                ),
        )

    companion object {
        fun emptyInstance() = CircleSummaryModels(listOf())
    }
}
