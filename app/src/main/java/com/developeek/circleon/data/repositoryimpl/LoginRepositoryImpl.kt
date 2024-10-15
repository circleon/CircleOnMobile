package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.remote.retrofit.service.LoginService

class LoginRepositoryImpl(
    private val service: LoginService,
) : LoginRepository {
    override suspend fun login(
        email: String,
        password: String,
    ): Result<Boolean> {
        // TODO: service.login(email, password)

        return Result.success(true)
    }

    override suspend fun signUp(
        email: String,
        userName: String,
        password: String,
    ): Result<Boolean> {
        // TODO: service.signUp(email, userName, password)

        return Result.success(true)
    }

    override suspend fun checkEmailDuplication(email: String): Result<Boolean> {
        // TODO: service.checkEmailDuplication(email)

        return Result.success(true)
    }

    override suspend fun requestEmailAuthenticationCode(email: String): Result<Boolean> {
        // TODO: service.requestEmailAuthenticationCode(email)

        return Result.success(true)
    }

    override suspend fun authenticateEmail(
        email: String,
        code: String,
    ): Result<Boolean> {
        // TODO: service.authenticateEmail(email, code)

        return Result.success(true)
    }
}
