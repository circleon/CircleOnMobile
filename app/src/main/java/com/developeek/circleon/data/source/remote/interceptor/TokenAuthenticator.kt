package com.developeek.circleon.data.source.remote.interceptor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    ) : Authenticator {
        private var requestRefreshJob: Job? = null
        private var requestAccessJob: Job? = null

        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            val refreshToken = tokenManager.getRefreshToken()

            if (refreshToken == null) {
                if (requestRefreshJobIsNullOrCompleted()) requestRefreshAndAccessToken()
            } else {
                if (requestAccessJobIsNullOrCompleted()) requestAccessToken()
            }

            return newRequest(tokenManager.getAccessToken()!!, response.request())
        }

        private fun requestRefreshJobIsNullOrCompleted() = requestRefreshJob == null || !(requestRefreshJob!!.isActive)

        private fun requestAccessJobIsNullOrCompleted() = requestAccessJob == null || !(requestAccessJob!!.isActive)

        private fun requestRefreshAndAccessToken() {
            requestRefreshJob =
                CoroutineScope(Dispatchers.IO).launch {
                    // TODO: request Refresh Token and set Refresh Token
                    // TODO: request Access Token and set Access Token
                    delay(100)
                }
        }

        private fun requestAccessToken() {
            requestAccessJob =
                CoroutineScope(Dispatchers.IO).launch {
                    // TODO: request Access Token and set Access Token
                    delay(100)
                }
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
