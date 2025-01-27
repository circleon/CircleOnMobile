package com.developeek.circleon.domain.utils.validator

import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.domain.vo.UserName
import java.io.IOException

object Validator {
    private const val MAX_COMMENT_SIZE = 255
    private const val MAX_POST_SIZE = 1000
    // 텍스트 유효성 검증은 즉각적인 피드백이 요구되기 때문에 UI 스레드에서 진행

    fun checkName(data: String): InputValidationResult<UserName> {
        return try {
            InputValidationResult.valid(UserName(data))
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(e)
        }
    }

    fun checkEmail(data: String): InputValidationResult<UserEmail> {
        return try {
            InputValidationResult.valid(UserEmail(data))
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(e)
        }
    }

    fun checkEmailAsId(data: String): InputValidationResult<UserEmail> {
        return try {
            InputValidationResult.valid(UserEmail(data))
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
    ): InputValidationResult<Unit> {
        return try {
            require(password == data)
            InputValidationResult.valid(Unit)
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(IOException(ValidatorExceptionMessage.MESSAGE_WRONG_PASSWORD_CHECK))
        }
    }

    fun checkComment(data: String): InputValidationResult<String> {
        checkEmpty(data)

        return try {
            require(data.length <= MAX_COMMENT_SIZE)
            InputValidationResult.valid(data)
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(
                IOException(
                    String.format(ValidatorExceptionMessage.MESSAGE_OVER_SIZE_COMMENT, MAX_COMMENT_SIZE),
                ),
            )
        }
    }

    fun checkPost(data: String): InputValidationResult<String> {
        checkEmpty(data)

        return try {
            require(data.length <= MAX_POST_SIZE)
            InputValidationResult.valid(data)
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(
                IOException(
                    String.format(ValidatorExceptionMessage.MESSAGE_OVER_SIZE_POST, MAX_POST_SIZE),
                ),
            )
        }
    }

    private fun checkEmpty(data: String): InputValidationResult<String> {
        return try {
            require(data.trim().isNotEmpty())
            InputValidationResult.valid(data)
        } catch (e: IllegalArgumentException) {
            InputValidationResult.invalid(IOException(ValidatorExceptionMessage.MESSAGE_EMPTY_CONTENT))
        }
    }
}
