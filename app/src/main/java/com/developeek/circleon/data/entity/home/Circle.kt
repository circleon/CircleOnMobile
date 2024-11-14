package com.developeek.circleon.data.entity.home

import com.developeek.circleon.BuildConfig
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleModel
import com.google.gson.annotations.SerializedName

data class Circles(
    val content: List<Circle>,
    val currentPageNumber: Int,
    val totalElementCount: Int,
    val totalPageCount: Int,
)

data class Circle(
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
            imageUrl(profileImgUrl),
            imageUrl(thumbnailUrl),
            category(category),
            memberCount,
        )

    private fun category(codeName: String): Category {
        val category = Category.findOrNull(codeName)

        return category ?: Category.ETC
    }

    private fun imageUrl(url: String?): String? {
        url ?: return null

        return BuildConfig.SERVICE_API_URL + IMAGE_PATH + url
    }

    companion object {
        private const val IMAGE_PATH = "circles/images/"
    }
}
