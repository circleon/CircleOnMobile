package com.developeek.circleon.data.repository

import com.developeek.circleon.data.source.Result
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.domain.vo.UserName

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Result<Unit>

    suspend fun signUp(
        email: UserEmail,
        userName: UserName,
        password: Password,
    ): Result<Unit>

    suspend fun requestEmailAuthenticationCode(email: UserEmail): Result<Unit>

    suspend fun authenticateEmail(
        email: UserEmail,
        code: String,
    ): Result<Unit>
}
