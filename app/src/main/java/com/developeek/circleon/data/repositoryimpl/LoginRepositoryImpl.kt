package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.entity.login.EmailAuthentication
import com.developeek.circleon.data.entity.login.Login
import com.developeek.circleon.data.entity.login.SignUp
import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.manager.TokenManager
import com.developeek.circleon.data.source.remote.retrofit.service.LoginService
import com.developeek.circleon.domain.model.UserModel
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.domain.vo.UserName
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
    ): Result<UserModel> {
        return try {
            withContext(dispatcher) {
                // TODO: 로그인 api 수정 후에 각각 User, Token 엔티티 정보 Manager 에 저장
                val response =
                    service.login(Login(email, password)).also {
                        tokenManager.setAccessToken(it.accessToken)
                        tokenManager.setRefreshToken(it.refreshToken)
                    }
                Result.success(response.toUserModel())
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
