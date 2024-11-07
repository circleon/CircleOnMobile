package com.developeek.circleon.data.entity.home

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleModel
import com.google.gson.annotations.SerializedName

data class CircleResponse(
    val content: List<CircleEntity>,
    val currentPageNumber: Int,
    val totalElementCount: Int,
    val totalPageCount: Int,
)

data class CircleEntity(
    @SerializedName("circleId") val id: Int,
    @SerializedName("circleName") val name: String,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val category: String,
    val memberCount: Int,
) {
    fun toCircleModel() =
        CircleModel(
            id,
            name,
            profileImgUrl,
            thumbnailUrl,
            category(category),
            memberCount,
        )

    private fun category(codeName: String): Category {
        val category = Category.findOrNull(codeName)

        return category ?: Category.ETC
    }
}
