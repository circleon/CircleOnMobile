package com.developeek.circleon.data.repository

import com.developeek.circleon.data.source.Result
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.model.PostModels
import java.io.File

interface CircleRepository {
    // GET
    suspend fun getCircles(
        page: Int,
        size: Int,
        category: Category,
    ): Result<CircleModels>

    suspend fun getCircleSummaries(): Result<CircleSummaryModels>

    suspend fun getCircleDetail(circleId: Int): Result<CircleDetailModel>

    suspend fun getCircleMembers(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<MemberModels>

    suspend fun getCircleLeaveRequestedMemberMessage(
        circleId: Int,
        memberId: Int,
    ): Result<String>

    suspend fun getCircleJoinRequestedMembers(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<MemberModels>

    suspend fun getCircleLeaveRequestedMembers(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<MemberModels>

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

    // POST
    suspend fun postMyCircle(circleId: Int): Result<Unit>

    suspend fun postCircleLeaveRequest(
        memberId: Int,
        leaveMessage: String,
    ): Result<Unit>

    suspend fun postCirclePost(
        circleId: Int,
        postType: PostType,
        content: String,
        image: File?,
    ): Result<Unit>

    suspend fun postCircleComment(
        circleId: Int,
        postId: Int,
        comment: String,
    ): Result<Unit>

    // PUT
    suspend fun putPostPin(
        circleId: Int,
        postId: Int,
        isPinned: Boolean,
    ): Result<Unit>

    suspend fun putCircle(circleDetailModel: CircleDetailModel): Result<Unit>

    suspend fun putCircleImage(
        circleId: Int,
        circleThumbnail: File?,
        circleIntroductionImage: File?,
    ): Result<Unit>

    suspend fun putCirclePost(
        circleId: Int,
        postId: Int,
        postType: PostType,
        content: String,
    ): Result<Unit>

    suspend fun putCircleComment(
        circleId: Int,
        postId: Int,
        commentId: Int,
        content: String,
    ): Result<Unit>

    suspend fun putCircleMemberRole(
        circleId: Int,
        memberId: Int,
        role: Role,
    ): Result<Unit>

    suspend fun putCircleMemberStatus(
        circleId: Int,
        memberId: Int,
        status: MembershipStatus,
    ): Result<Unit>

    // DELETE
    suspend fun deleteCircleImage(
        circleId: Int,
        deleteThumbnail: Boolean,
        deleteIntroductionImage: Boolean,
    ): Result<Unit>

    suspend fun deleteCirclePost(
        circleId: Int,
        postId: Int,
    ): Result<Unit>

    suspend fun deletePostComment(
        circleId: Int,
        postId: Int,
        commentId: Int,
    ): Result<Unit>

    suspend fun deleteCircleMember(
        circleId: Int,
        memberId: Int,
    ): Result<Unit>
}
