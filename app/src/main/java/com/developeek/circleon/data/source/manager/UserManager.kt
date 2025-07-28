package com.developeek.circleon.data.source.manager

import android.content.SharedPreferences
import com.developeek.circleon.data.di.UserSharedPreferences
import com.developeek.circleon.data.dto.auth.User
import com.developeek.circleon.domain.model.UserModel
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager
    @Inject
    constructor(
        @UserSharedPreferences private val preferences: SharedPreferences,
    ) {
        private val editor = preferences.edit()

        fun getUser(): UserModel {
            preferences.let {
                val id = it.getInt(USER_ID_KEY, 0)
                val name = it.getString(USER_NAME_KEY, null)
                val univCode = it.getString(USER_UNIV_KEY, null)
                val profileImgUrl = it.getString(USER_PROFILE_IMAGE_KEY, null)

                if (name == null || univCode == null) {
                    throw IOException("No User Data")
                }

                return User(id, name, univCode, profileImgUrl).toUserModel()
            }
        }

        fun setUser(user: User) {
            user.let {
                editor.putInt(USER_ID_KEY, it.id)
                editor.putString(USER_NAME_KEY, it.name)
                editor.putString(USER_UNIV_KEY, it.univCode)
                editor.putString(USER_PROFILE_IMAGE_KEY, it.profileImgUrl)
                editor.apply()
            }
        }

        fun deleteUser() {
            editor.clear()
            editor.apply()
        }

        companion object {
            private const val USER_ID_KEY = "userId"
            private const val USER_NAME_KEY = "userName"
            private const val USER_UNIV_KEY = "userUniv"
            private const val USER_PROFILE_IMAGE_KEY = "userProfileImg"
        }
    }
