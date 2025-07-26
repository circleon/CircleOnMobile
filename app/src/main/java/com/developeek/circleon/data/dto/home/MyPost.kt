package com.developeek.circleon.data.dto.home

import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.model.MyPostModel
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.utils.Utils
import com.google.gson.annotations.SerializedName

data class MyPost(
    val circleId: Int,
    val circleName: String,
    @SerializedName("postId") val id: Int,
    @SerializedName("postType") val type: String,
    val isPinned: Boolean,
    @SerializedName("postImgUrl") val imgUrl: String?,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val commentCount: Int,
    val author: Author,
) {
    fun toMyPostModel() =
        MyPostModel(
            circleId,
            circleName,
            PostModel(
                id,
                PostType.findOrDefault(type),
                isPinned,
                Utils.getPostImageUrlOrNull(imgUrl),
                content,
                Utils.getLocalDateTimeOrDefault(createdAt),
                Utils.getLocalDateTimeOrDefault(updatedAt),
                commentCount,
                author.toAuthorModel(),
            ),
        )
}
