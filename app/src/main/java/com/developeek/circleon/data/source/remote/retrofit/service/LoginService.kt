package com.developeek.circleon.data.source.remote.retrofit.service

import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("auth/login")
    suspend fun login(
        @Body email: String,
        @Body password: String,
    )
}
