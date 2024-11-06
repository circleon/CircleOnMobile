package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.entity.home.CircleResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CircleService {
    @GET("api/circles")
    suspend fun getCircles(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
        @Query("categoryType") category: String,
    ): CircleResponse

    @GET("api/circles")
    suspend fun getAllCircles(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): CircleResponse
}
