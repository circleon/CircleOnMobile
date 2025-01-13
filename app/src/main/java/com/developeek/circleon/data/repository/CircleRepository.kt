package com.developeek.circleon.data.repository

import com.developeek.circleon.data.source.Result
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.model.PostModels

interface CircleRepository {
    // GET
    suspend fun getCircles(
        page: Int,
        size: Int,
        category: Category,
    ): Result<CircleModels>

    suspend fun getCircleSummaries(): Result<CircleSummaryModels>

    suspend fun getCircleDetail(circleId: Int): Result<CircleDetailModel>

    suspend fun getCirclePosts(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<PostModels>

    suspend fun getCircleNotices(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<PostModels>

    suspend fun getPostComments(
        circleId: Int,
        postId: Int,
        page: Int,
        size: Int,
    ): Result<CommentModels>

    // PUT
    suspend fun putPostPin(
        circleId: Int,
        postId: Int,
        isPinned: Boolean,
    ): Result<Unit>

    // POST
    suspend fun postComment(
        circleId: Int,
        postId: Int,
        comment: String,
    ): Result<CommentModel>

    // DELETE
    suspend fun deletePost(
        circleId: Int,
        postId: Int,
    ): Result<Unit>
}
