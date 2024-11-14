package com.developeek.circleon.data.source.remote.retrofit.service

import com.developeek.circleon.data.entity.login.Email
import com.developeek.circleon.data.entity.login.EmailAuthentication
import com.developeek.circleon.data.entity.login.Login
import com.developeek.circleon.data.entity.login.SignUp
import com.developeek.circleon.data.entity.login.User
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("auth/login")
    suspend fun login(
        @Body data: Login,
    ): User

    @POST("auth/signup")
    suspend fun signUp(
        @Body data: SignUp,
    )

    @POST("auth/verification")
    suspend fun requestEmailAuthenticationCode(
        @Body data: Email,
    )

    @POST("auth/verification-code")
    suspend fun authenticateEmail(
        @Body data: EmailAuthentication,
    )
}
