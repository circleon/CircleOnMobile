package com.developeek.circleon.view.viewmodelimpl.auth

import com.developeek.circleon.data.dto.auth.PolicyId
import com.developeek.circleon.data.dto.auth.PublicId
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Valid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail

class PasswordChangeManager {
    val email: UserEmail
        get() = _email
    private lateinit var _email: UserEmail
    private var emailCondition = false
    private var emailAuthenticated = false
    private var emailValidationMessage = Const.EMPTY_TEXT

    val password: Password
        get() = _password
    private lateinit var _password: Password
    private var passwordCondition = false
    private var passwordCheckCondition = false
    private var passwordValidationMessage = Const.EMPTY_TEXT

    val policyId: PolicyId
        get() = _policyId
    private lateinit var _policyId: PolicyId
    val publicId: PublicId
        get() = _publicId
    private lateinit var _publicId: PublicId

    fun validateAndSetEmail(email: String) {
        emailAuthenticated = false

        when (val result = Validator.checkEmail(email)) {
            is Valid -> {
                _email = result.data
                emailCondition = true
            }
            is Invalid -> {
                emailValidationMessage = result.message()
                emailCondition = false
            }
        }
    }

    fun setAsEmailAuthenticated() {
        emailAuthenticated = true
    }

    fun setPolicyId(policyId: PolicyId) {
        _policyId = policyId
    }

    fun setPublicId(publicId: PublicId) {
        _publicId = publicId
    }

    fun validateAndSetPassword(password: String) {
        passwordCheckCondition = false

        when (val result = Validator.checkPassword(password)) {
            is Valid -> {
                _password = result.data
                passwordCondition = true
                passwordValidationMessage = Const.EMPTY_TEXT
            }
            is Invalid -> {
                passwordValidationMessage = result.message()
                passwordCondition = false
            }
        }
    }

    fun setPasswordCheck(
        password: String,
        passwordCheck: String,
    ) {
        when (Validator.checkPasswordMatch(password, passwordCheck)) {
            is Valid -> {
                passwordCheckCondition = true
            }
            is Invalid -> {
                passwordCheckCondition = false
            }
        }
    }

    fun getConditionByChangePasswordStep(step: ChangePasswordStep) =
        when (step) {
            ChangePasswordStep.EMAIL -> emailCondition
            ChangePasswordStep.EMAIL_AUTHENTICATION -> emailAuthenticated
            ChangePasswordStep.PASSWORD -> passwordCondition && passwordCheckCondition
        }

    fun getValidationMessageByChangePasswordStep(step: ChangePasswordStep) =
        when (step) {
            ChangePasswordStep.EMAIL -> emailValidationMessage
            ChangePasswordStep.EMAIL_AUTHENTICATION -> Const.EMPTY_TEXT
            ChangePasswordStep.PASSWORD -> passwordValidationMessage
        }
}
