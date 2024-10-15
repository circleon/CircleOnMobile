package com.developeek.circleon.data.repository

import com.developeek.circleon.data.Result

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
}
