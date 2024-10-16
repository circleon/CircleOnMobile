package com.developeek.circleon.domain.vo

import com.developeek.circleon.data.exception.ExceptionMessage
import java.io.Serializable

data class Password(
    private val data: String,
) : Serializable {
    init {
        require(isNotEmpty()) { String.format(ExceptionMessage.MESSAGE_INPUT_VALIDATION_NO_DATA, PASSWORD) }
        require(isPasswordFormat()) { String.format(ExceptionMessage.MESSAGE_WRONG_FORMAT_PASSWORD) }
    }

    private fun get() = data

    private fun isNotEmpty() = data.isNotEmpty()

    private fun isPasswordFormat(): Boolean {
        val pattern = Regex("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,12}\$")

        return pattern.matches(data)
    }

    fun check(password: String) {
        require(data == password) { String.format(ExceptionMessage.MESSAGE_WRONG_FORMAT_EMAIL) }
    }

    companion object {
        private const val PASSWORD = "비밀번호"
    }
}
