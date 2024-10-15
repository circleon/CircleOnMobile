package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.Result
import com.developeek.circleon.data.repository.LoginRepository
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
}
