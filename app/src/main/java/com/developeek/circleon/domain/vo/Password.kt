package com.developeek.circleon.domain.vo

import com.developeek.circleon.domain.utils.validator.ValidatorExceptionMessage
import java.io.Serializable

data class Password(
    private val data: String,
) : Serializable {
    init {
        require(isNotEmpty()) { String.format(ValidatorExceptionMessage.MESSAGE_INPUT_VALIDATION_NO_DATA, PASSWORD) }
        require(isPasswordFormat()) { String.format(ValidatorExceptionMessage.MESSAGE_WRONG_FORMAT) }
    }

    fun get() = data

    fun check(password: String) {
        require(data == password) { String.format(ValidatorExceptionMessage.MESSAGE_WRONG_PASSWORD_CHECK) }
    }

    private fun isNotEmpty() = data.isNotEmpty()

    private fun isPasswordFormat(): Boolean {
        val pattern = Regex("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,12}\$")

        return pattern.matches(data)
    }

    companion object {
        private const val PASSWORD = "비밀번호"
    }
}
