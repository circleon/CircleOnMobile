package com.developeek.circleon.data.source.manager

import android.content.SharedPreferences
import com.developeek.circleon.data.di.TokenSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager
    @Inject
    constructor(
        @TokenSharedPreferences private val preferences: SharedPreferences,
    ) {
        fun getAccessToken(): String? {
            return preferences.getString(ACCESS_TOKEN_KEY, null)
        }

        fun getRefreshToken(): String? {
            return preferences.getString(REFRESH_TOKEN_KEY, null)
        }

        fun setAccessToken(token: String) {
            return with(preferences.edit()) {
                putString(ACCESS_TOKEN_KEY, token)
            }.apply()
        }

        fun setRefreshToken(token: String) {
            with(preferences.edit()) {
                putString(REFRESH_TOKEN_KEY, token)
            }.apply()
        }

        fun deleteAccessToken() {
            with(preferences.edit()) {
                remove(ACCESS_TOKEN_KEY)
            }.apply()
        }

        fun deleteRefreshToken() {
            with(preferences.edit()) {
                remove(REFRESH_TOKEN_KEY)
            }.apply()
        }

        companion object {
            private const val ACCESS_TOKEN_KEY = "accessToken"
            private const val REFRESH_TOKEN_KEY = "refreshToken"
        }
    }
