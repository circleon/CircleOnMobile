package com.developeek.circleon.domain.utils.validator

import com.developeek.circleon.domain.vo.Email
import com.developeek.circleon.domain.vo.Name
import com.developeek.circleon.domain.vo.Password
import java.io.IOException

object Validator {
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

    fun checkEmailAsId(data: String): InputValidationResult<Email> {
        return try {
            InputValidationResult.valid(Email(data))
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(IOException(ValidatorExceptionMessage.MESSAGE_WRONG_FORMAT_ID))
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
                throw IllegalArgumentException(String.format(ValidatorExceptionMessage.MESSAGE_WRONG_PASSWORD_CHECK))
            }
            password.check(data)
            InputValidationResult.valid(true)
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(e)
        }
    }
}
