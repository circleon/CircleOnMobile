package com.developeek.circleon.view.viewmodelimpl.auth

import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Valid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.domain.vo.Password
import com.developeek.circleon.domain.vo.UserEmail
import com.developeek.circleon.domain.vo.UserName

class SignUpManager {
    val name: UserName
        get() = _name
    private lateinit var _name: UserName
    val nameStringValue: String
        get() = _nameStringValue
    private var _nameStringValue = Const.EMPTY_TEXT
    private var nameCondition = false
    private var nameValidationMessage = Const.EMPTY_TEXT

    val email: UserEmail
        get() = _email
    private lateinit var _email: UserEmail
    val emailStringValue: String
        get() = _emailStringValue
    private var _emailStringValue = Const.EMPTY_TEXT
    private var emailCondition = false
    private var emailAuthenticated = false
    private var emailValidationMessage = Const.EMPTY_TEXT

    val password: Password
        get() = _password
    private lateinit var _password: Password
    val passwordStringValue: String
        get() = _passwordStringValue
    private var _passwordStringValue = Const.EMPTY_TEXT
    val passwordCheckStringValue: String
        get() = _passwordCheckStringValue
    private var _passwordCheckStringValue = Const.EMPTY_TEXT
    private var passwordCondition = false
    private var passwordCheckCondition = false
    private var passwordValidationMessage = Const.EMPTY_TEXT

    fun validateAndSetName(name: String) {
        _nameStringValue = name

        when (val result = Validator.checkName(name)) {
            is Valid -> {
                _name = result.data
                nameCondition = true
            }
            is Invalid -> {
                nameValidationMessage = result.message()
                nameCondition = false
            }
        }
    }

    fun validateAndSetEmail(email: String) {
        _emailStringValue = email
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

    fun validateAndSetPassword(password: String) {
        _passwordStringValue = password
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

        setPasswordCheck(passwordCheckStringValue)
    }

    fun setPasswordCheck(password: String) {
        _passwordCheckStringValue = password

        when (Validator.checkPasswordMatch(passwordStringValue, password)) {
            is Valid -> {
                passwordCheckCondition = true
            }
            is Invalid -> {
                passwordCheckCondition = false
            }
        }
    }

    fun setAsEmailAuthenticated() {
        emailAuthenticated = true
    }

    fun getConditionBySignUpStep(step: SignUpStep) =
        when (step) {
            SignUpStep.NAME -> true
            SignUpStep.EMAIL -> true
            SignUpStep.EMAIL_AUTHENTICATION -> true
            SignUpStep.PASSWORD -> passwordCondition && passwordCheckCondition
            SignUpStep.TERMS -> TODO()
        }

    fun getValidationMessageBySignUpStep(step: SignUpStep) =
        when (step) {
            SignUpStep.NAME -> nameValidationMessage
            SignUpStep.EMAIL -> emailValidationMessage
            SignUpStep.EMAIL_AUTHENTICATION -> "*인증을 완료해주세요"
            SignUpStep.PASSWORD -> passwordValidationMessage
            SignUpStep.TERMS -> TODO()
        }
}
