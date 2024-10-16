package com.developeek.circleon.data.repository

import com.developeek.circleon.data.source.Result
import com.developeek.circleon.domain.vo.Email
import com.developeek.circleon.domain.vo.Name
import com.developeek.circleon.domain.vo.Password

interface LoginRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Result<Boolean>

    suspend fun signUp(
        email: Email,
        userName: Name,
        password: Password,
    ): Result<Boolean>

    suspend fun requestEmailAuthenticationCode(email: Email): Result<Boolean>

    suspend fun authenticateEmail(
        email: Email,
        code: String,
    ): Result<Boolean>
}
