package com.developeek.circleon.data.dto.home

import com.developeek.circleon.domain.model.AuthorModel
import com.developeek.circleon.domain.utils.Utils
import com.google.gson.annotations.SerializedName

data class Author(
    @SerializedName("authorId") val id: Int,
    @SerializedName("authorName") val name: String,
    @SerializedName("authorProfileUrl") val profileImgUrl: String?,
) {
    fun toAuthorModel() =
        AuthorModel(
            id,
            name,
            Utils.getUserImageUrlOrNull(profileImgUrl),
        )
}
