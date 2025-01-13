package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.dto.home.Circle
import com.developeek.circleon.data.dto.home.CircleDetail
import com.developeek.circleon.data.dto.home.CircleSummaries
import com.developeek.circleon.data.dto.home.Comment
import com.developeek.circleon.data.dto.home.CommentContent
import com.developeek.circleon.data.dto.home.Paging
import com.developeek.circleon.data.dto.home.Pin
import com.developeek.circleon.data.dto.home.Post
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("circles/{id}")
    suspend fun getCircleDetail(
        @Path("id") circleId: Int,
    ): CircleDetail

    @GET("circles/{id}/posts")
    suspend fun getCirclePosts(
        @Path("id") circleId: Int,
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

    // PUT
    @PUT("circles/{circleId}/posts/{postId}/pin")
    suspend fun putPostPin(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Body data: Pin,
    )

    // POST
    @POST("circles/{circleId}/posts/{postId}/comments")
    suspend fun postComment(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
        @Body data: CommentContent,
    ): Comment

    // DELETE
    @DELETE("circles/{circleId}/posts/{postId}")
    suspend fun deletePost(
        @Path("circleId") circleId: Int,
        @Path("postId") postId: Int,
    )
}
