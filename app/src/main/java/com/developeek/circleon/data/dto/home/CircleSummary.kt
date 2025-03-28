package com.developeek.circleon.data.dto.home

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.utils.Utils
import com.google.gson.annotations.SerializedName

data class CircleSummaries(
    val content: List<CircleSummary>,
)

data class CircleSummary(
    @SerializedName("circleId") val id: Int,
    @SerializedName("circleName") val name: String,
    @SerializedName("categoryType") val category: String,
    val thumbnailUrl: String?,
) {
    fun toCircleSummaryModel() =
        CircleSummaryModel(
            id,
            name,
            Category.findOrDefault(category),
            Utils.getCircleImageUrlOrNull(thumbnailUrl),
        )
}
