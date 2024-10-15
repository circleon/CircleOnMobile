package com.developeek.circleon.data.repository

import com.developeek.circleon.data.source.Result

interface LoginRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Result<Boolean>

    suspend fun signUp(
        email: String,
        userName: String,
        password: String,
    ): Result<Boolean>

    suspend fun checkEmailDuplication(email: String): Result<Boolean>

    suspend fun requestEmailAuthenticationCode(email: String): Result<Boolean>

    suspend fun authenticateEmail(
        email: String,
        code: String,
    ): Result<Boolean>
}
