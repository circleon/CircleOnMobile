package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.entity.login.AccessToken
import com.developeek.circleon.data.entity.login.RefreshToken
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {
    @POST("auth/refresh")
    suspend fun refreshAccessToken(
        @Body data: RefreshToken,
    ): AccessToken
}
