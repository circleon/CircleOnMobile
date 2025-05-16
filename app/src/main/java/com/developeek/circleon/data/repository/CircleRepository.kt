package com.developeek.circleon.data.repository

import com.developeek.circleon.data.source.Result
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.model.MyPostModel
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.view.viewmodelimpl.Page
import java.io.File

interface CircleRepository {
    // GET
    suspend fun getCircles(
        page: Int,
        size: Int,
        category: Category,
    ): Result<Page<CircleModel>>

    suspend fun getCircleSummaries(): Result<CircleSummaryModels>

    suspend fun getCircleDetail(circleId: Int): Result<CircleDetailModel>

    suspend fun getCircleMembers(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<MemberModels>

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

    suspend fun getCircleJoinRequestedMemberMessage(
        circleId: Int,
        memberId: Int,
    ): Result<String>

    suspend fun getCircleLeaveRequestedMemberMessage(
        circleId: Int,
        memberId: Int,
    ): Result<String>

    suspend fun getCirclePosts(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<Page<PostModel>>

    suspend fun getCircleNotices(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<Page<PostModel>>

    suspend fun getCirclePostComments(
        circleId: Int,
        postId: Int,
        page: Int,
        size: Int,
    ): Result<CommentModels>

    suspend fun getMyCircles(
        page: Int,
        size: Int,
    ): Result<CircleSummaryModels>

    suspend fun getMyJoinRequestedCircles(
        page: Int,
        size: Int,
    ): Result<CircleSummaryModels>

    suspend fun getMyLeaveRequestedCircles(
        page: Int,
        size: Int,
    ): Result<CircleSummaryModels>

    suspend fun getMyPosts(
        page: Int,
        size: Int,
    ): Result<Page<MyPostModel>>

    suspend fun getMyCommentPosts(
        page: Int,
        size: Int,
    ): Result<Page<MyPostModel>>

    // POST
    suspend fun postCircle(
        circleDetailModel: CircleDetailModel,
        profileImg: File?,
        introductionImg: File?,
    ): Result<Unit>

    suspend fun postMyCircle(
        circleId: Int,
        joinMessage: String,
    ): Result<Unit>

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

    suspend fun postCirclePostComment(
        circleId: Int,
        postId: Int,
        comment: String,
    ): Result<Unit>

    suspend fun postReportCircle(
        circleId: Int,
        content: String,
    ): Result<Unit>

    suspend fun postReportCirclePost(
        circleId: Int,
        postId: Int,
        content: String,
    ): Result<Unit>

    suspend fun postReportCirclePostComment(
        circleId: Int,
        commentId: Int,
        content: String,
    ): Result<Unit>

    // PUT
    suspend fun putPostPin(
        circleId: Int,
        postId: Int,
        isPinned: Boolean,
    ): Result<Unit>

    suspend fun putCircle(circleDetailModel: CircleDetailModel): Result<Unit>

    suspend fun putCircleOfficialStatus(circleId: Int): Result<Unit>

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

    suspend fun putCirclePostComment(
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

    suspend fun deleteCirclePostComment(
        circleId: Int,
        postId: Int,
        commentId: Int,
    ): Result<Unit>

    suspend fun deleteCircleMember(
        circleId: Int,
        memberId: Int,
    ): Result<Unit>

    suspend fun deleteCircleJoinRequest(memberId: Int): Result<Unit>
}
