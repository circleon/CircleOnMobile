package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.dto.login.EmailAuthentication
import com.developeek.circleon.data.dto.login.Login
import com.developeek.circleon.data.dto.login.LogoutRequestBody
import com.developeek.circleon.data.dto.login.SignUp
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

class LoginRepositoryImpl(
    private val service: LoginService,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val dispatcher: CoroutineDispatcher,
) : LoginRepository {
    override suspend fun login(
        email: String,
        password: String,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.login(Login(email, password)).also {
                    val user = it.user
                    val token = it.token

                    userManager.setUser(user.id, user.name, user.univCode)
                    tokenManager.setAccessToken(token.accessToken)
                    tokenManager.setRefreshToken(token.refreshToken)
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.getRefreshToken()?.let {
                service.logout(LogoutRequestBody(it))
            }
            tokenManager.deleteAccessToken()
            tokenManager.deleteRefreshToken()
            userManager.deleteUser()
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
            withContext(dispatcher) {
                service.signUp(SignUp(email.get(), userName.get(), password.get()))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            return Result.error(e)
        }
    }

    override suspend fun requestEmailAuthenticationCode(email: UserEmail): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.requestEmailAuthenticationCode(com.developeek.circleon.data.dto.login.Email(email.get()))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            return Result.error(e)
        }
    }

    override suspend fun authenticateEmail(
        email: UserEmail,
        code: String,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.authenticateEmail(EmailAuthentication(email.get(), code))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            return Result.error(e)
        }
    }
}
