package com.developeek.circleon.data.dto.login

import com.developeek.circleon.domain.enums.University
import com.developeek.circleon.domain.model.UserModel
import com.developeek.circleon.domain.utils.Utils
import com.google.gson.annotations.SerializedName
import java.io.IOException

data class User(
    @SerializedName("userId") val id: Int,
    @SerializedName("username") val name: String,
    val univCode: String,
    val profileImgUrl: String?,
) {
    fun toUserModel() =
        UserModel(
            id,
            name,
            univ(univCode),
            Utils.getUserImageUrlOrNull(profileImgUrl),
        )

    private fun univ(univCode: String) =
        University.findOrNull(univCode)
            ?: throw IOException(MESSAGE_WRONG_UNIV_CODE)

    companion object {
        private const val MESSAGE_WRONG_UNIV_CODE = "대학교 정보가 올바르지 않습니다 "
    }
}
