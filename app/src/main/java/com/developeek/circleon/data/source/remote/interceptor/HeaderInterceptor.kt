package com.developeek.circleon.data.source.remote.interceptor

import com.developeek.circleon.data.source.manager.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor
    @Inject
    constructor(private val tokenManager: TokenManager) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val accessToken = tokenManager.getAccessToken()
            val request =
                if (accessToken == null) {
                    chain.request()
                } else {
                    chain.request().putTokenHeader(accessToken)
                }
            return chain.proceed(request)
        }

        private fun Request.putTokenHeader(accessToken: String): Request {
            return this.newBuilder()
                .addHeader(AUTHORIZATION, "Bearer $accessToken")
                .build()
        }

        companion object {
            private const val AUTHORIZATION = "Authorization"
        }
    }
