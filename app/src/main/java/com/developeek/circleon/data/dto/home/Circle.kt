package com.developeek.circleon.data.dto.home

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.utils.Utils
import com.google.gson.annotations.SerializedName

data class Circle(
    @SerializedName("circleId") val id: Int,
    @SerializedName("circleName") val name: String,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val category: String,
    @SerializedName("summary") val comment: String,
    val memberCount: Int,
) {
    fun toCircleModel() =
        CircleModel(
            id,
            name,
            Utils.getCircleImageUrlOrNull(profileImgUrl),
            Utils.getCircleImageUrlOrNull(thumbnailUrl),
            Category.findOrDefault(category),
            comment,
            memberCount,
        )
}
