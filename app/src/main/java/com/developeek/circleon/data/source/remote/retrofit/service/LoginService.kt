package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.entity.login.EmailAuthenticationEntity
import com.developeek.circleon.data.entity.login.EmailEntity
import com.developeek.circleon.data.entity.login.LoginEntity
import com.developeek.circleon.data.entity.login.SignUpEntity
import com.developeek.circleon.data.entity.login.TokenEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("auth/login")
    suspend fun login(
        @Body data: LoginEntity,
    ): TokenEntity

    @POST("auth/signup")
    suspend fun signUp(
        @Body data: SignUpEntity,
    )

    @POST("auth/duplication")
    suspend fun checkEmailDuplication(
        @Body data: EmailEntity,
    )

    @POST("auth/verification")
    suspend fun requestEmailAuthenticationCode(
        @Body data: EmailEntity,
    )

    @POST("auth/verification-code")
    suspend fun authenticateEmail(
        @Body data: EmailAuthenticationEntity,
    )
}
