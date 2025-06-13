package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.dto.auth.EmailAuthenticationRequestBody
import com.developeek.circleon.data.dto.auth.EmailCodeRequestBody
import com.developeek.circleon.data.dto.auth.Login
import com.developeek.circleon.data.dto.auth.NewPasswordEmailAuthenticationRequestBody
import com.developeek.circleon.data.dto.auth.NewPasswordRequestBody
import com.developeek.circleon.data.dto.auth.PolicyId
import com.developeek.circleon.data.dto.auth.PublicId
import com.developeek.circleon.data.dto.auth.SignUpRequestBody
import com.developeek.circleon.data.repository.AuthRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.manager.TokenManager
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.data.source.remote.retrofit.service.AuthService
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.domain.vo.UserName

class AuthRepositoryImpl(
    private val service: AuthService,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
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
            Result.error(e)
        }
    }

    override suspend fun changePassword(
        publicId: PublicId,
        password: Password,
    ): Result<Unit> {
        return try {
            service.changePassword(NewPasswordRequestBody(publicId.data, password.get()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun requestEmailAuthenticationCode(email: UserEmail): Result<Unit> {
        return try {
            service.requestEmailAuthenticationCode(EmailCodeRequestBody(email.get()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun requestEmailAuthenticationCodeForNewPassword(email: UserEmail): Result<PolicyId> {
        return try {
            val response = service.requestEmailAuthenticationCodeForNewPassword(EmailCodeRequestBody(email.get()))
            Result.success(PolicyId(response.data))
        } catch (e: Exception) {
            Result.error(e)
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
            Result.error(e)
        }
    }

    override suspend fun authenticateEmailForNewPassword(
        policyId: PolicyId,
        code: String,
    ): Result<PublicId> {
        return try {
            val response =
                service.authenticateEmailForNewPassword(
                    NewPasswordEmailAuthenticationRequestBody(policyId.data, code),
                )
            return Result.success(PublicId(response.data))
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}
