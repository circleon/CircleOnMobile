package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.entity.home.CircleDetail
import com.developeek.circleon.data.entity.home.CircleSummaries
import com.developeek.circleon.data.entity.home.Circles
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CircleService {
    @GET("circles")
    suspend fun getCircles(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
        @Query("categoryType") category: String,
    ): Circles

    @GET("circles")
    suspend fun getAllCircles(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): Circles

    @GET("circles/summary")
    suspend fun getCircleSummaries(): CircleSummaries

    @GET("circle/{id}")
    suspend fun getCircleDetail(
        @Path("id") circleId: Int,
    ): CircleDetail
}
