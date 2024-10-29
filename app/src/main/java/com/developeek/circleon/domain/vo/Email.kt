package com.developeek.circleon.domain.vo

import android.util.Patterns
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

    private fun isEmailFormat() = Patterns.EMAIL_ADDRESS.matcher(data).matches()

    companion object {
        private const val EMAIL = "이메일"
    }
}
