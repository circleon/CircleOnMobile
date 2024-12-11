package com.developeek.circleon.data.dto.home

import com.developeek.circleon.domain.model.AuthorModel
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.utils.Utils.circleImageUrl
import com.developeek.circleon.domain.utils.Utils.postImageUrl
import com.google.gson.annotations.SerializedName
import java.time.LocalTime

data class Post(
    @SerializedName("postId") val id: Int,
    val isPinned: Boolean,
    val postImgUrl: String?,
    val content: String,
    val createdAt: LocalTime,
    val updatedAt: LocalTime,
    val commentCount: Int,
    val author: Author,
) {
    fun toPostModel() =
        PostModel(
            id,
            isPinned,
            postImageUrl(postImgUrl),
            content,
            createdAt,
            updatedAt,
            commentCount,
            author.toAuthorModel(),
        )
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

data class Posts(
    val content: List<Post>,
    val currentPageNumber: Int,
    val totalElementCount: Int,
    val totalPageCount: Int,
) {
    fun isLastPage() = currentPageNumber >= (totalPageCount - 1)
}
