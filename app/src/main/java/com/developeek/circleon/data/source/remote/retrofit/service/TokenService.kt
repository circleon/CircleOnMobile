package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.entity.login.AccessTokenEntity
import com.developeek.circleon.data.entity.login.RefreshTokenEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {
    @POST("auth/refresh")
    suspend fun refreshAccessToken(
        @Body data: RefreshTokenEntity,
    ): AccessTokenEntity
}
