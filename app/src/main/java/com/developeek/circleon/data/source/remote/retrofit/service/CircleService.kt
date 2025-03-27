package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.dto.home.Circle
import com.developeek.circleon.data.dto.home.CircleDetail
import com.developeek.circleon.data.dto.home.CircleSummaries
import com.developeek.circleon.data.dto.home.Comment
import com.developeek.circleon.data.dto.home.Member
import com.developeek.circleon.data.dto.home.Paging
import com.developeek.circleon.data.dto.home.Pin
import com.developeek.circleon.data.dto.home.Post
import com.developeek.circleon.data.dto.home.RequestBodyEditCircleDetail
import com.developeek.circleon.data.dto.home.RequestBodyEditComment
import com.developeek.circleon.data.dto.home.RequestBodyEditMemberRole
import com.developeek.circleon.data.dto.home.RequestBodyEditMemberStatus
import com.developeek.circleon.data.dto.home.RequestBodyEditPost
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
    ): Paging<Circle>

    // TODO: categoryType 생략 대신에 nullable 로 처리 가능한지? 되면은 getCircle 하나로 통합 가능
    @GET("circles")
    suspend fun getAllCircles(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): Paging<Circle>

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
    ): Paging<Member>

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
    ): Paging<Post>

    @GET("circles/{circleId}/posts/{postId}/comments")
    suspend fun getPostComments(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Paging<Comment>

    // POST
    @POST("my-circles/{circleId}")
    suspend fun postMyCircle(
        @Path("circleId") circleId: Int,
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
    suspend fun postCircleComment(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Body data: RequestBodyEditComment,
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
    suspend fun putCircleComment(
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
    suspend fun deletePostComment(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Path("commentId") commentId: Int,
    )

    @DELETE("circles/{circleId}/members/{memberId}")
    suspend fun deleteCircleMember(
        @Path("circleId") circleId: Int,
        @Path("memberId") memberId: Int,
    )
}
