package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import java.io.Serializable
import kotlin.math.absoluteValue

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

    companion object {
        fun findByKeyword(
            models: Models<CircleSummaryModel>,
            keyword: String,
        ) = Models(
            models.get().filter { it.name.lowercase().contains(keyword.lowercase()) }
                .sortedWith(
                    compareBy<CircleSummaryModel> { it.name.compareTo(keyword).absoluteValue }
                        .thenBy { it.name }
                        .thenBy { it.category },
                ),
        )
    }
}
