package com.developeek.circleon.domain.vo

import com.developeek.circleon.domain.utils.validator.ValidatorExceptionMessage
import java.io.Serializable

data class Email(
    private val data: String,
) : Serializable {
    init {
        require(isNotEmpty()) { String.format(ValidatorExceptionMessage.MESSAGE_INPUT_VALIDATION_NO_DATA, EMAIL) }
        require(isEmailFormat()) { String.format(ValidatorExceptionMessage.MESSAGE_WRONG_FORMAT) }
    }

    fun get() = data

    private fun isNotEmpty() = data.isNotEmpty()

    private fun isEmailFormat(): Boolean {
        val pattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")

        return pattern.matches(data)
    }

    companion object {
        private const val EMAIL = "이메일"
    }
}
