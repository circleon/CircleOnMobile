package com.developeek.circleon.data.source.manager

import android.content.SharedPreferences
import com.developeek.circleon.data.di.UserSharedPreferences
import com.developeek.circleon.data.dto.login.User
import com.developeek.circleon.domain.model.UserModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager
    @Inject
    constructor(
        @UserSharedPreferences private val preferences: SharedPreferences,
    ) {
        private var user: UserModel? = null

        fun getUser(): UserModel? {
            val id = preferences.getInt(USER_ID_KEY, 0)
            val name = preferences.getString(USER_NAME_KEY, null)
            val univCode = preferences.getString(USER_UNIV_KEY, null)

            if (name != null && univCode != null) {
                user = User(id, name, univCode).toUserModel()
            }

            return user
        }

        fun setUser(
            id: Int,
            name: String,
            univCode: String,
        ) {
            val editor = preferences.edit()

            editor.putInt(USER_ID_KEY, id)
            editor.putString(USER_NAME_KEY, name)
            editor.putString(USER_UNIV_KEY, univCode)
            editor.apply()
        }

        companion object {
            private const val USER_ID_KEY = "userId"
            private const val USER_NAME_KEY = "userName"
            private const val USER_UNIV_KEY = "userUniv"
        }
    }
