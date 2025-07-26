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
    private var nameCondition = false
    private var nameValidationMessage = Const.EMPTY_TEXT

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

    val hasAgreedServiceTerms: Boolean
        get() = _hasAgreedServiceTerms
    private var _hasAgreedServiceTerms = false
    val hasAgreedPrivacyPolicies: Boolean
        get() = _hasAgreedPrivacyPolicies
    private var _hasAgreedPrivacyPolicies = false
    val hasAgreedCommunityRules: Boolean
        get() = _hasAgreedCommunityRules
    private var _hasAgreedCommunityRules = false
    val hasAgreedAllTerms: Boolean
        get() = _hasAgreedServiceTerms && _hasAgreedPrivacyPolicies && _hasAgreedCommunityRules

    fun validateAndSetName(name: String) {
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

    fun toggleAllTermsAgreement() {
        hasAgreedAllTerms.let {
            _hasAgreedServiceTerms = !it
            _hasAgreedPrivacyPolicies = !it
            _hasAgreedCommunityRules = !it
        }
    }

    fun toggleServiceTermsAgreement() {
        _hasAgreedServiceTerms = !_hasAgreedServiceTerms
    }

    fun togglePrivacyPolicyAgreement() {
        _hasAgreedPrivacyPolicies = !_hasAgreedPrivacyPolicies
    }

    fun toggleCommunityRulesAgreement() {
        _hasAgreedCommunityRules = !_hasAgreedCommunityRules
    }

    fun getConditionBySignUpStep(step: SignUpStep) =
        when (step) {
            SignUpStep.NAME -> nameCondition
            SignUpStep.EMAIL -> emailCondition
            SignUpStep.EMAIL_AUTHENTICATION -> emailAuthenticated
            SignUpStep.PASSWORD -> passwordCondition && passwordCheckCondition
            SignUpStep.TERMS -> _hasAgreedServiceTerms && _hasAgreedPrivacyPolicies && _hasAgreedCommunityRules
        }

    fun getValidationMessageBySignUpStep(step: SignUpStep) =
        when (step) {
            SignUpStep.NAME -> nameValidationMessage
            SignUpStep.EMAIL -> emailValidationMessage
            SignUpStep.EMAIL_AUTHENTICATION -> Const.EMPTY_TEXT
            SignUpStep.PASSWORD -> passwordValidationMessage
            SignUpStep.TERMS -> Const.EMPTY_TEXT
        }
}
