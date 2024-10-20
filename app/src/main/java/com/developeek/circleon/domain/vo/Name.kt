package com.developeek.circleon.domain.vo

import com.developeek.circleon.domain.utils.validator.ValidatorExceptionMessage
import java.io.Serializable

data class Name(
    private val data: String,
) : Serializable {
    init {
        require(isNotEmpty()) { String.format(ValidatorExceptionMessage.MESSAGE_INPUT_VALIDATION_NO_DATA, NAME) }
    }

    fun get() = data

    private fun isNotEmpty() = data.isNotEmpty()

    companion object {
        private const val NAME = "이름"
    }
}
