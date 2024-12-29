package com.developeek.circleon.data.dto.home

import android.util.TimeFormatException
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.model.AuthorModel
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.Utils.circleImageUrl
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class Posts(
    val content: List<Post>,
    val currentPageNumber: Int,
    val totalElementCount: Int,
    val totalPageCount: Int,
) {
    fun isLastPage() = currentPageNumber >= (totalPageCount - 1)
}

data class Post(
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
    fun toPostModel() =
        PostModel(
            id,
            PostType.findOrDefault(type),
            isPinned,
            Utils.postImageUrl(imgUrl),
            content,
            localDateTime(createdAt),
            localDateTime(updatedAt),
            commentCount,
            author.toAuthorModel(),
        )

    private fun localDateTime(dateTime: String) =
        try {
            LocalDateTime.parse(dateTime)
        } catch (e: TimeFormatException) {
            LocalDateTime.of(0, 0, 0, 0, 0)
        }
}

data class Author(
    @SerializedName("authorId") val id: Int,
    @SerializedName("authorName") val name: String,
    @SerializedName("authorProfileUrl") val profileUrl: String?,
) {
    fun toAuthorModel() =
        AuthorModel(
            id,
            name,
            circleImageUrl(profileUrl),
        )
}

data class Pin(
    val isPinned: Boolean,
)
