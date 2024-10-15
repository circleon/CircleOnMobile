package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.entity.LoginEntity
import com.developeek.circleon.data.entity.SignUpEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("auth/login")
    suspend fun login(
        @Body data: LoginEntity,
    )

    @POST("auth/signup")
    suspend fun signUp(
        @Body data: SignUpEntity,
    )
}
