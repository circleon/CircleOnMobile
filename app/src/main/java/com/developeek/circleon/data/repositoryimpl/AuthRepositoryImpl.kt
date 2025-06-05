package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.dto.auth.EmailAuthenticationRequestBody
import com.developeek.circleon.data.dto.auth.EmailCodeRequestBody
import com.developeek.circleon.data.dto.auth.Login
import com.developeek.circleon.data.dto.auth.SignUpRequestBody
import com.developeek.circleon.data.repository.AuthRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.manager.TokenManager
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.data.source.remote.retrofit.service.AuthService
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.domain.vo.UserName
import kotlinx.coroutines.CoroutineDispatcher

class AuthRepositoryImpl(
    private val service: AuthService,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val dispatcher: CoroutineDispatcher,
) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String,
    ): Result<Unit> {
        return try {
            service.login(Login(email, password)).also {
                val user = it.user
                val token = it.token

                userManager.setUser(user)
                tokenManager.setAccessToken(token.accessToken)
                tokenManager.setRefreshToken(token.refreshToken)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun signUp(
        email: UserEmail,
        userName: UserName,
        password: Password,
    ): Result<Unit> {
        return try {
            service.signUp(SignUpRequestBody(email.get(), userName.get(), password.get()))
            Result.success(Unit)
        } catch (e: Exception) {
            return Result.error(e)
        }
    }

    override suspend fun requestEmailAuthenticationCode(email: UserEmail): Result<Unit> {
        return try {
            service.requestEmailAuthenticationCode(EmailCodeRequestBody(email.get()))
            Result.success(Unit)
        } catch (e: Exception) {
            return Result.error(e)
        }
    }

    override suspend fun authenticateEmail(
        email: UserEmail,
        code: String,
    ): Result<Unit> {
        return try {
            service.authenticateEmail(EmailAuthenticationRequestBody(email.get(), code))
            Result.success(Unit)
        } catch (e: Exception) {
            return Result.error(e)
        }
    }
}
