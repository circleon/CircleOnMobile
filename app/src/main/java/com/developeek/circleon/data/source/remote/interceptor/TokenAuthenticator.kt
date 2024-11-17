package com.developeek.circleon.data.source.remote.interceptor

import com.developeek.circleon.data.entity.login.RefreshToken
import com.developeek.circleon.data.exception.ServiceException
import com.developeek.circleon.data.exception.ServiceExceptionMessage
import com.developeek.circleon.data.source.manager.TokenManager
import com.developeek.circleon.data.source.remote.retrofit.StatusCode
import com.developeek.circleon.data.source.remote.retrofit.service.TokenService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import org.json.JSONTokener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator
    @Inject
    constructor(
        private val tokenManager: TokenManager,
        private val service: TokenService,
    ) : Authenticator {
        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            val refreshToken = tokenManager.getRefreshToken() ?: throw ServiceException.RefreshTokenExpiredException("")
            val responseString = response.body()!!.string()
            val jsonObject = JSONTokener(responseString).nextValue() as JSONObject
            if (jsonObject.getString(PARAM_NAME_ERROR_CODE) == StatusCode.FAIL_REFRESH_TOKEN_VALIDATION.errorCode()) {
                throw ServiceException.RefreshTokenExpiredException(ServiceExceptionMessage.MESSAGE_REFRESH_EXPIRED)
            }

            return runBlocking {
                try {
                    tokenManager.setAccessToken(requestAccessToken(refreshToken))
                } catch (e: Exception) {
                    throw ServiceException.RefreshTokenExpiredException(ServiceExceptionMessage.MESSAGE_REFRESH_EXPIRED)
                }
                if (tokenManager.getAccessToken() == null) {
                    null
                } else {
                    newRequest(tokenManager.getAccessToken()!!, response.request())
                }
            }
        }

        private suspend fun requestAccessToken(refreshToken: String): String {
            val response = service.refreshAccessToken(RefreshToken(refreshToken))
            return response.accessToken
        }

        private fun newRequest(
            token: String,
            request: Request,
        ) = request.newBuilder()
            .header(AUTHORIZATION, "Bearer $token")
            .build()

        companion object {
            private const val AUTHORIZATION = "Authorization"
            private const val PARAM_NAME_ERROR_CODE = "errorCode"
        }
    }
