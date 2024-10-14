package com.developeek.circleon.data.repository

import com.developeek.circleon.data.Result

interface LoginRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Result<Boolean>
}
