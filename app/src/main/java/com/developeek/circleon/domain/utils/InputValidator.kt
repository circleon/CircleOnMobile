package com.developeek.circleon.domain.utils

import com.developeek.circleon.data.exception.ExceptionMessage
import com.developeek.circleon.domain.vo.Email
import com.developeek.circleon.domain.vo.Name
import com.developeek.circleon.domain.vo.Password

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

    fun checkPassword(data: String): InputValidationResult<Password> {
        return try {
            InputValidationResult.valid(Password(data))
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(e)
        }
    }

    fun checkPasswordMatch(
        password: Password?,
        data: String,
    ): InputValidationResult<Boolean> {
        return try {
            if (password == null) {
                throw IllegalArgumentException(String.format(ExceptionMessage.MESSAGE_WRONG_FORMAT_EMAIL))
            }
            password.check(data)
            InputValidationResult.valid(true)
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(e)
        }
    }
}
