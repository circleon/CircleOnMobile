package com.developeek.circleon.domain.utils

import com.developeek.circleon.domain.vo.Email
import com.developeek.circleon.domain.vo.Name

object InputValidator {
    fun checkName(data: String): InputValidationResult<Name> {
        return try {
            InputValidationResult.valid(Name(data))
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(e)
        }
    }

    fun checkEmail(data: String): InputValidationResult<Email> {
        return try {
            InputValidationResult.valid(Email(data))
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(e)
        }
    }
}
