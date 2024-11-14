package com.developeek.circleon.data.repository

import com.developeek.circleon.data.source.Result
import com.developeek.circleon.domain.model.UserModel
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.domain.vo.UserName

interface LoginRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Result<UserModel>

    suspend fun signUp(
        email: UserEmail,
        userName: UserName,
        password: Password,
    ): Result<Boolean>

    suspend fun requestEmailAuthenticationCode(email: UserEmail): Result<Boolean>

    suspend fun authenticateEmail(
        email: UserEmail,
        code: String,
    ): Result<Boolean>
}
