package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.remote.retrofit.service.LoginService
import com.developeek.circleon.domain.vo.Email
import com.developeek.circleon.domain.vo.Name
import com.developeek.circleon.domain.vo.Password

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
        email: Email,
        userName: Name,
        password: Password,
    ): Result<Boolean> {
        // TODO: service.signUp(email, userName, password)

        return Result.success(true)
    }

    override suspend fun requestEmailAuthenticationCode(email: Email): Result<Boolean> {
        // TODO: service.requestEmailAuthenticationCode(email)

        return Result.success(true)
    }

    override suspend fun authenticateEmail(
        email: Email,
        code: String,
    ): Result<Boolean> {
        // TODO: service.authenticateEmail(email, code)

        return Result.success(true)
    }
}
