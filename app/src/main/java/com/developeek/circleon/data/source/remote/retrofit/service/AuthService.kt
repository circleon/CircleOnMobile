package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.dto.auth.EmailAuthenticationRequestBody
import com.developeek.circleon.data.dto.auth.EmailCodeRequestBody
import com.developeek.circleon.data.dto.auth.Login
import com.developeek.circleon.data.dto.auth.LoginResult
import com.developeek.circleon.data.dto.auth.SignUpRequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    // POST
    @POST("auth/login")
    suspend fun login(
        @Body data: Login,
    ): LoginResult

    @POST("auth/signup")
    suspend fun signUp(
        @Body data: SignUpRequestBody,
    )

    @POST("auth/verification")
    suspend fun requestEmailAuthenticationCode(
        @Body data: EmailCodeRequestBody,
    )

    @POST("auth/verification-code")
    suspend fun authenticateEmail(
        @Body data: EmailAuthenticationRequestBody,
    )
}
