package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.entity.login.EmailAuthentication
import com.developeek.circleon.data.entity.login.Login
import com.developeek.circleon.data.entity.login.SignUp
import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.manager.TokenManager
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.data.source.remote.retrofit.service.LoginService
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.domain.vo.UserName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException

class LoginRepositoryImpl(
    private val service: LoginService,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val dispatcher: CoroutineDispatcher,
) : LoginRepository {
    override suspend fun login(
        email: String,
        password: String,
    ): Result<Boolean> {
        return try {
            withContext(dispatcher) {
                service.login(Login(email, password)).also {
                    val user = it.user
                    val token = it.token

                    userManager.setUser(user.id, user.name, user.univCode)
                    tokenManager.setAccessToken(token.accessToken)
                    tokenManager.setRefreshToken(token.refreshToken)
                }
                Result.success(true)
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun signUp(
        email: UserEmail,
        userName: UserName,
        password: Password,
    ): Result<Boolean> {
        return try {
            withContext(dispatcher) {
                service.signUp(SignUp(email.get(), userName.get(), password.get()))
                Result.success(true)
            }
        } catch (e: IOException) {
            return Result.error(e)
        }
    }

    override suspend fun requestEmailAuthenticationCode(email: UserEmail): Result<Boolean> {
        return try {
            withContext(dispatcher) {
                service.requestEmailAuthenticationCode(com.developeek.circleon.data.entity.login.Email(email.get()))
                Result.success(true)
            }
        } catch (e: IOException) {
            return Result.error(e)
        }
    }

    override suspend fun authenticateEmail(
        email: UserEmail,
        code: String,
    ): Result<Boolean> {
        return try {
            withContext(dispatcher) {
                service.authenticateEmail(EmailAuthentication(email.get(), code))
                Result.success(true)
            }
        } catch (e: IOException) {
            return Result.error(e)
        }
    }
}
