package com.developeek.circleon.data.source.remote.interceptor

import com.developeek.circleon.data.entity.login.RefreshTokenEntity
import com.developeek.circleon.data.source.remote.retrofit.service.TokenService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator
    @Inject
    constructor(
        private val tokenManager: TokenManager,
        private val tokenRequester: TokenRequester,
        private val service: TokenService,
    ) : Authenticator {
        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            val refreshToken = tokenManager.getRefreshToken() ?: return null

            val request = tokenRequester.get()
            if (request === tokenRequester.first()) {
                return runBlocking {
                    tokenManager.setAccessToken(requestAccessToken(refreshToken))
                    if (tokenManager.getAccessToken() == null) {
                        null
                    } else {
                        newRequest(tokenManager.getAccessToken()!!, response.request())
                    }
                }
            } else {
                return newRequest(tokenManager.getAccessToken()!!, response.request())
            }
        }

        private suspend fun requestAccessToken(refreshToken: String): String {
            val response = service.refreshAccessToken(RefreshTokenEntity(refreshToken))
            return response.accessToken
        }

        private fun newRequest(
            token: String,
            request: Request,
        ) = request.newBuilder()
            .header(AUTHORIZATION, token)
            .build()

        companion object {
            private const val AUTHORIZATION = "authorization"
        }
    }
