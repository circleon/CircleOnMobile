package com.developeek.circleon.data.source.remote.interceptor

import kotlinx.coroutines.delay
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
    ) : Authenticator {
        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            val refreshToken = tokenManager.getRefreshToken() ?: return null

            return runBlocking {
                val request = tokenRequester.get()

                if (request != null) {
                    if (request === tokenRequester.first()) {
                        tokenManager.setAccessToken(requestAccessToken(refreshToken))
                    }

                    if (tokenManager.getAccessToken() == null) {
                        null
                    } else {
                        newRequest(tokenManager.getAccessToken()!!, response.request())
                    }
                } else {
                    null
                }
            }
        }

        private suspend fun requestAccessToken(refreshToken: String): String {
            // TODO: request access token api
            delay(100)

            return ""
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
