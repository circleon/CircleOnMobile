package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.dto.auth.LogoutRequestBody
import com.developeek.circleon.data.dto.auth.User
import com.developeek.circleon.data.dto.home.CircleSummary
import com.developeek.circleon.data.dto.home.MyPost
import com.developeek.circleon.data.dto.home.Page
import com.developeek.circleon.data.dto.home.RequestResponseBodyCircleJoin
import com.developeek.circleon.data.dto.home.RequestResponseBodyCircleLeave
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    // GET
    @GET("users/me")
    suspend fun getUser(): User

    @GET("my-circles")
    suspend fun getMyCircles(
        @Query("membershipStatus") membershipStatus: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Page<CircleSummary>

    @GET("users/me/posts")
    suspend fun getMyPosts(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): Page<MyPost>

    @GET("users/me/commented-posts")
    suspend fun getMyCommentPosts(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): Page<MyPost>

    // POST
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

    // PUT
    @Multipart
    @PUT("users/me/image")
    suspend fun putUserProfileImage(
        @Part image: MultipartBody.Part?,
    )

    // DELETE
    @DELETE("my-circles/{memberId}/application")
    suspend fun deleteCircleJoinRequest(
        @Path("memberId") memberId: Int,
    )

    @DELETE("users/me/image")
    suspend fun deleteUserProfileImage()

    @POST("auth/logout")
    suspend fun logout(
        @Body data: LogoutRequestBody,
    )
}
