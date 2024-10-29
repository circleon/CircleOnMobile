package com.developeek.circleon.domain.utils.validator

import com.developeek.circleon.domain.vo.Email
import com.developeek.circleon.domain.vo.Name
import com.developeek.circleon.domain.vo.Password
import java.io.IOException

object Validator {
    // 텍스트 유효성 검증은 즉각적인 피드백이 요구되기 때문에 UI 스레드에서 진행

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
        password: String,
        data: String,
    ): InputValidationResult<Boolean> {
        return try {
            if (password != data) {
                throw IllegalArgumentException(String.format(ValidatorExceptionMessage.MESSAGE_WRONG_PASSWORD_CHECK))
            }
            InputValidationResult.valid(true)
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(e)
        }
    }
}
