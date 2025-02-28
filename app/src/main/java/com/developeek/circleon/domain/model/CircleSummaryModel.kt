package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import java.io.Serializable
import kotlin.math.absoluteValue

data class CircleSummaryModels(private val models: List<CircleSummaryModel>) {
    fun get() = models

    fun get(index: Int) = models[index]

    fun isEmpty() = models.isEmpty()

    fun find(keyword: String) =
        CircleSummaryModels(
            models.filter { it.name.lowercase().contains(keyword.lowercase()) }
                .sortedWith(
                    compareBy<CircleSummaryModel> { it.name.compareTo(keyword).absoluteValue }
                        .thenBy { it.name }
                        .thenBy { it.category },
                ),
        )

    companion object {
        fun empty() = CircleSummaryModels(emptyList())
    }
}

data class CircleSummaryModel(
    val id: Int,
    val name: String,
    val category: Category,
) : Serializable {
    fun isSame(circleSummaryModel: CircleSummaryModel) = this.id == circleSummaryModel.id

    fun areContentsSame(circleSummaryModel: CircleSummaryModel) = this == circleSummaryModel
}
