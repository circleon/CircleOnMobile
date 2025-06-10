package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.dto.auth.AccessToken
import com.developeek.circleon.data.dto.auth.RefreshToken
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {
    // POST
    @POST("auth/refresh")
    suspend fun refreshAccessToken(
        @Body data: RefreshToken,
    ): AccessToken
}
