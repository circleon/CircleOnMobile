package com.developeek.circleon.domain.vo

import com.developeek.circleon.data.exception.ExceptionMessage
import java.io.Serializable

data class Name(
    private val data: String,
) : Serializable {
    init {
        require(isNotEmpty()) { String.format(ExceptionMessage.MESSAGE_INPUT_VALIDATION_NO_DATA, NAME) }
    }

    private fun get() = data

    private fun isNotEmpty() = data.isNotEmpty()

    companion object {
        private const val NAME = "이름"
    }
}
