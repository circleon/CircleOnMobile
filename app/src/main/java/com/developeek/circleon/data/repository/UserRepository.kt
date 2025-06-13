package com.developeek.circleon.data.repository

import com.developeek.circleon.data.source.Result
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.model.MyPostModel
import java.io.File

interface UserRepository {
    // GET
    suspend fun getMyCircles(
        page: Int,
        size: Int,
    ): Result<Models<CircleSummaryModel>>

    suspend fun getMyJoinRequestedCircles(
        page: Int,
        size: Int,
    ): Result<Models<CircleSummaryModel>>

    suspend fun getMyLeaveRequestedCircles(
        page: Int,
        size: Int,
    ): Result<Models<CircleSummaryModel>>

    suspend fun getMyPosts(
        page: Int,
        size: Int,
    ): Result<Page<MyPostModel>>

    suspend fun getMyCommentPosts(
        page: Int,
        size: Int,
    ): Result<Page<MyPostModel>>

    // POST
    suspend fun postMyCircle(
        circleId: Int,
        joinMessage: String,
    ): Result<Unit>

    suspend fun postCircleLeaveRequest(
        memberId: Int,
        leaveMessage: String,
    ): Result<Unit>

    // PUT
    suspend fun putUser(): Result<Unit>

    suspend fun putUserProfileImage(profileImage: File?): Result<Unit>

    // DELETE
    suspend fun deleteCircleJoinRequest(memberId: Int): Result<Unit>

    suspend fun deleteUserProfileImage(): Result<Unit>

    suspend fun logout(): Result<Unit>

    suspend fun resign(): Result<Unit>
}
