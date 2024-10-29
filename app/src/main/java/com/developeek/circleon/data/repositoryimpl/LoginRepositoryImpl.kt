package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.entity.login.EmailAuthenticationEntity
import com.developeek.circleon.data.entity.login.EmailEntity
import com.developeek.circleon.data.entity.login.LoginEntity
import com.developeek.circleon.data.entity.login.SignUpEntity
import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.remote.interceptor.TokenManager
import com.developeek.circleon.data.source.remote.retrofit.service.LoginService
import com.developeek.circleon.domain.vo.Email
import com.developeek.circleon.domain.vo.Name
import com.developeek.circleon.domain.vo.Password
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException

class LoginRepositoryImpl(
    private val service: LoginService,
    private val tokenManager: TokenManager,
    private val dispatcher: CoroutineDispatcher,
) : LoginRepository {
    override suspend fun login(
        email: String,
        password: String,
    ): Result<Boolean> {
        return try {
            withContext(dispatcher) {
                service.login(LoginEntity(email, password)).also {
                    tokenManager.setAccessToken(it.accessToken)
                    tokenManager.setRefreshToken(it.refreshToken)
                }
                Result.success(true)
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun signUp(
        email: Email,
        userName: Name,
        password: Password,
    ): Result<Boolean> {
        return try {
            withContext(dispatcher) {
                service.signUp(SignUpEntity(email.get(), userName.get(), password.get()))
                Result.success(true)
            }
        } catch (e: IOException) {
            return Result.error(e)
        }
    }

    override suspend fun requestEmailAuthenticationCode(email: Email): Result<Boolean> {
        return try {
            withContext(dispatcher) {
                service.requestEmailAuthenticationCode(EmailEntity(email.get()))
                Result.success(true)
            }
        } catch (e: IOException) {
            return Result.error(e)
        }
    }

    override suspend fun authenticateEmail(
        email: Email,
        code: String,
    ): Result<Boolean> {
        return try {
            withContext(dispatcher) {
                service.authenticateEmail(EmailAuthenticationEntity(email.get(), code))
                Result.success(true)
            }
        } catch (e: IOException) {
            return Result.error(e)
        }
    }
}
