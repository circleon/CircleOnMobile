package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.dto.home.Circle
import com.developeek.circleon.data.dto.home.CircleDetail
import com.developeek.circleon.data.dto.home.CircleSummaries
import com.developeek.circleon.data.dto.home.CircleSummary
import com.developeek.circleon.data.dto.home.Comment
import com.developeek.circleon.data.dto.home.Member
import com.developeek.circleon.data.dto.home.Page
import com.developeek.circleon.data.dto.home.Pin
import com.developeek.circleon.data.dto.home.Post
import com.developeek.circleon.data.dto.home.RequestBodyEditCircleDetail
import com.developeek.circleon.data.dto.home.RequestBodyEditComment
import com.developeek.circleon.data.dto.home.RequestBodyEditMemberRole
import com.developeek.circleon.data.dto.home.RequestBodyEditMemberStatus
import com.developeek.circleon.data.dto.home.RequestBodyEditPost
import com.developeek.circleon.data.dto.home.RequestBodyReport
import com.developeek.circleon.data.dto.home.RequestResponseBodyCircleJoin
import com.developeek.circleon.data.dto.home.RequestResponseBodyCircleLeave
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface CircleService {
    // GET
    @GET("circles")
    suspend fun getCircleScrollContents(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
        @Query("categoryType") category: String,
    ): Page<Circle>

    // TODO: categoryType 생략 대신에 nullable 로 처리 가능한지? 되면은 getCircle 하나로 통합 가능
    @GET("circles")
    suspend fun getAllCircles(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): Page<Circle>

    @GET("circles/summary")
    suspend fun getCircleSummaries(): CircleSummaries

    @GET("circles/{circleId}")
    suspend fun getCircleDetail(
        @Path("circleId") circleId: Int,
    ): CircleDetail

    @GET("circles/{circleId}/members")
    suspend fun getMembers(
        @Path("circleId") circleId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
        @Query("membershipStatus") membershipStatus: String,
    ): Page<Member>

    @GET("circles/{circleId}/members/{memberId}/join-message")
    suspend fun getCircleJoinRequestedMemberMessage(
        @Path("circleId") circleId: Int,
        @Path("memberId") memberId: Int,
    ): RequestResponseBodyCircleJoin

    @GET("circles/{circleId}/members/{memberId}/leave-message")
    suspend fun getCircleLeaveRequestedMemberMessage(
        @Path("circleId") circleId: Int,
        @Path("memberId") memberId: Int,
    ): RequestResponseBodyCircleLeave

    @GET("circles/{circleId}/posts")
    suspend fun getCirclePosts(
        @Path("circleId") circleId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("postType") postType: String,
    ): Page<Post>

    @GET("circles/{circleId}/posts/{postId}/comments")
    suspend fun getCirclePostComments(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Page<Comment>

    @GET("my-circles")
    suspend fun getMyCircles(
        @Query("membershipStatus") membershipStatus: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Page<CircleSummary>

    // POST
    @Multipart
    @POST("circles")
    suspend fun postCircle(
        @Part("circleName") circleName: RequestBody,
        @Part("summary") summary: RequestBody,
        @Part("category") category: RequestBody,
        @Part("introduction") introduction: RequestBody?,
        @Part("recruitmentStartDate") recruitmentStartDate: RequestBody?,
        @Part("recruitmentEndDate") recruitmentEndDate: RequestBody?,
        @Part profileImg: MultipartBody.Part?,
        @Part introductionImg: MultipartBody.Part?,
    )

    @POST("my-circles/{circleId}")
    suspend fun postMyCircle(
        @Path("circleId") circleId: Int,
        @Body data: RequestResponseBodyCircleJoin,
    )

    @POST("my-circles/{memberId}/leave-request")
    suspend fun postCircleLeaveRequest(
        @Path("memberId") memberId: Int,
        @Body data: RequestResponseBodyCircleLeave,
    )

    @Multipart
    @POST("circles/{circleId}/posts")
    suspend fun postCirclePost(
        @Path("circleId") circleId: Int,
        @Part("postType") postType: RequestBody,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?,
    )

    @POST("circles/{circleId}/posts/{postId}/comments")
    suspend fun postCirclePostComment(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Body data: RequestBodyEditComment,
    )

    @POST("circles/{circleId}/reports")
    suspend fun postReportCircle(
        @Path("circleId") circleId: Int,
        @Body data: RequestBodyReport,
    )

    @POST("circles/{circleId}/posts/{postId}/reports")
    suspend fun postReportCirclePost(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Body data: RequestBodyReport,
    )

    @POST("circles/{circleId}/comments/{commentId}/reports")
    suspend fun postReportCirclePostComment(
        @Path("circleId") circleId: Int,
        @Path("commentId") commentId: Int,
        @Body data: RequestBodyReport,
    )

    // PUT
    @PUT("circles/{circleId}/posts/{postId}/pin")
    suspend fun putPostPin(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Body data: Pin,
    )

    @PUT("circles/{circleId}")
    suspend fun putCircle(
        @Path("circleId") circleId: Int,
        @Body data: RequestBodyEditCircleDetail,
    )

    @Multipart
    @PUT("circles/{circleId}/images")
    suspend fun putCircleImage(
        @Path("circleId") circleId: Int,
        @Part thumbnail: MultipartBody.Part?,
        @Part introductionImage: MultipartBody.Part?,
    )

    @PUT("circles/{circleId}/posts/{postId}")
    suspend fun putCirclePost(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Body data: RequestBodyEditPost,
    )

    @PUT("circles/{circleId}/posts/{postId}/comments/{commentId}")
    suspend fun putCirclePostComment(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Path("commentId") commentId: Int,
        @Body data: RequestBodyEditComment,
    )

    @PUT("circles/{circleId}/members/{memberId}/role")
    suspend fun putCircleMemberRole(
        @Path("circleId") circleId: Int,
        @Path("memberId") memberId: Int,
        @Body data: RequestBodyEditMemberRole,
    )

    @PUT("circles/{circleId}/members/{memberId}/status")
    suspend fun putCircleMemberStatus(
        @Path("circleId") circleId: Int,
        @Path("memberId") memberId: Int,
        @Body data: RequestBodyEditMemberStatus,
    )

    // DELETE
    @DELETE("circles/{circleId}/images")
    suspend fun deleteCircleImage(
        @Path("circleId") circleId: Int,
        @Query("deleteProfileImg") deleteProfileImg: Boolean,
        @Query("deleteIntroImg") deleteIntroImg: Boolean,
    )

    @DELETE("circles/{circleId}/posts/{postId}")
    suspend fun deleteCirclePost(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
    )

    @DELETE("circles/{circleId}/posts/{postId}/comments/{commentId}")
    suspend fun deleteCirclePostComment(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Path("commentId") commentId: Int,
    )

    @DELETE("circles/{circleId}/members/{memberId}")
    suspend fun deleteCircleMember(
        @Path("circleId") circleId: Int,
        @Path("memberId") memberId: Int,
    )

    @DELETE("my-circles/{memberId}/application")
    suspend fun deleteCircleJoinRequest(
        @Path("memberId") memberId: Int,
    )
}
