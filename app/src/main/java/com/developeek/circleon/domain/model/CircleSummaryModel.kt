package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import java.io.Serializable
import kotlin.math.absoluteValue

data class CircleSummaryModels(private val models: List<CircleSummaryModel>) : Serializable {
    fun get() = models

    fun get(index: Int) = models[index]

    fun size() = models.size

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
    val circleId: Int,
    val name: String,
    val category: Category,
    val thumbnailUrl: String?,
    val memberId: Int,
) : BaseModel(circleId), Serializable {
    override fun areContentsSame(target: BaseModel): Boolean {
        if (target !is CircleSummaryModel) return false

        return this == target
    }
}
