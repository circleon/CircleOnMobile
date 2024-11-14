package com.developeek.circleon.data.source.manager

import android.content.SharedPreferences
import com.developeek.circleon.data.di.UserSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager
    @Inject
    constructor(
        @UserSharedPreferences private val preferences: SharedPreferences,
    ) {
        fun getUser() {
            // TODO: 정보 없을 때 RefreshException throw
            preferences.getInt(USER_ID_KEY, 0)
            preferences.getString(USER_NAME_KEY, "")
            preferences.getString(USER_UNIV_KEY, "")
        }

        companion object {
            private const val USER_ID_KEY = "userId"
            private const val USER_NAME_KEY = "userName"
            private const val USER_UNIV_KEY = "userUniv"
        }
    }
