package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.dto.home.Circle
import com.developeek.circleon.data.dto.home.CircleDetail
import com.developeek.circleon.data.dto.home.CircleSummaries
import com.developeek.circleon.data.dto.home.Comment
import com.developeek.circleon.data.dto.home.Paging
import com.developeek.circleon.data.dto.home.Pin
import com.developeek.circleon.data.dto.home.Post
import com.developeek.circleon.data.dto.home.RequestBodyComment
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
        @Body data: RequestBodyComment,
    )

    // PUT
    @PUT("circles/{circleId}/posts/{postId}/pin")
    suspend fun putPostPin(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Body data: Pin,
    )

    // DELETE
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
}
