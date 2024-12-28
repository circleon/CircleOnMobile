package com.developeek.circleon.data.dto.home

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.google.gson.annotations.SerializedName

data class CircleSummaries(
    val content: List<CircleSummary>,
)

data class CircleSummary(
    @SerializedName("circleId") val id: Int,
    @SerializedName("circleName") val name: String,
    @SerializedName("categoryType") val category: String,
) {
    fun toCircleSummaryModel() =
        CircleSummaryModel(
            id,
            name,
            Category.findOrDefault(category),
        )
}
