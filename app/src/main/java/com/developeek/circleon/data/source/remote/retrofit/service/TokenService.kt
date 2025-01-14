package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.dto.login.AccessToken
import com.developeek.circleon.data.dto.login.RefreshToken
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {
    // POST
    @POST("auth/refresh")
    suspend fun refreshAccessToken(
        @Body data: RefreshToken,
    ): AccessToken
}
