package com.developeek.circleon

import com.developeek.circleon.data.dto.auth.Login
import com.developeek.circleon.data.dto.auth.Token
import com.developeek.circleon.data.dto.auth.AccessToken
import com.developeek.circleon.data.dto.auth.RefreshToken
import org.junit.Assert.*
import org.junit.Test

/**
 * Specific unit tests for CircleOn DTOs (Data Transfer Objects).
 * Testing framework: JUnit 4
 * 
 * These tests focus specifically on the DTO classes found in the codebase:
 * - Login DTO
 * - Token, AccessToken, RefreshToken DTOs
 * - User DTO behavior simulation
 */
class DtoTest {

    // Login DTO comprehensive tests
    @Test
    fun login_constructor_setsPropertiesCorrectly() {
        val email = "test@circleon.com"
        val password = "testPassword123"
        
        val login = Login(email, password)
        
        assertEquals("Email property should be set correctly", email, login.email)
        assertEquals("Password property should be set correctly", password, login.password)
    }

    @Test
    fun login_dataClass_componentFunctions_workCorrectly() {
        val login = Login("user@test.com", "password123")
        
        val (email, password) = login
        
        assertEquals("Component1 should return email", "user@test.com", email)
        assertEquals("Component2 should return password", "password123", password)
    }

    @Test
    fun login_copy_function_worksCorrectly() {
        val originalLogin = Login("original@test.com", "originalPass")
        
        val copiedLogin = originalLogin.copy(email = "copied@test.com")
        
        assertEquals("Copied login should have new email", "copied@test.com", copiedLogin.email)
        assertEquals("Copied login should retain original password", "originalPass", copiedLogin.password)
        assertNotEquals("Original and copied should not be equal", originalLogin, copiedLogin)
    }

    @Test
    fun login_equals_worksCorrectly() {
        val login1 = Login("same@test.com", "samePassword")
        val login2 = Login("same@test.com", "samePassword")
        val login3 = Login("different@test.com", "samePassword")
        val login4 = Login("same@test.com", "differentPassword")
        
        assertEquals("Same data should be equal", login1, login2)
        assertNotEquals("Different email should not be equal", login1, login3)
        assertNotEquals("Different password should not be equal", login1, login4)
    }

    @Test
    fun login_hashCode_isConsistent() {
        val login1 = Login("test@example.com", "testPass")
        val login2 = Login("test@example.com", "testPass")
        
        assertEquals("Equal objects should have same hashCode", login1.hashCode(), login2.hashCode())
        
        // Multiple calls should return same hashCode
        val hash1 = login1.hashCode()
        val hash2 = login1.hashCode()
        assertEquals("Multiple hashCode calls should return same value", hash1, hash2)
    }

    // Token DTO comprehensive tests
    @Test
    fun token_constructor_setsPropertiesCorrectly() {
        val accessToken = "access_token_value"
        val refreshToken = "refresh_token_value"
        
        val token = Token(accessToken, refreshToken)
        
        assertEquals("Access token should be set correctly", accessToken, token.accessToken)
        assertEquals("Refresh token should be set correctly", refreshToken, token.refreshToken)
    }

    @Test
    fun token_dataClass_componentFunctions_workCorrectly() {
        val token = Token("access123", "refresh456")
        
        val (accessToken, refreshToken) = token
        
        assertEquals("Component1 should return accessToken", "access123", accessToken)
        assertEquals("Component2 should return refreshToken", "refresh456", refreshToken)
    }

    @Test
    fun token_copy_function_worksCorrectly() {
        val originalToken = Token("originalAccess", "originalRefresh")
        
        val copiedToken = originalToken.copy(accessToken = "newAccess")
        
        assertEquals("Copied token should have new access token", "newAccess", copiedToken.accessToken)
        assertEquals("Copied token should retain original refresh token", "originalRefresh", copiedToken.refreshToken)
        assertNotEquals("Original and copied should not be equal", originalToken, copiedToken)
    }

    @Test
    fun token_equals_worksCorrectly() {
        val token1 = Token("access1", "refresh1")
        val token2 = Token("access1", "refresh1")
        val token3 = Token("access2", "refresh1")
        val token4 = Token("access1", "refresh2")
        
        assertEquals("Same data should be equal", token1, token2)
        assertNotEquals("Different access token should not be equal", token1, token3)
        assertNotEquals("Different refresh token should not be equal", token1, token4)
    }

    // AccessToken DTO tests
    @Test
    fun accessToken_constructor_setsPropertyCorrectly() {
        val tokenValue = "access_token_123456"
        
        val accessToken = AccessToken(tokenValue)
        
        assertEquals("Access token value should be set correctly", tokenValue, accessToken.accessToken)
    }

    @Test
    fun accessToken_dataClass_behaviors_workCorrectly() {
        val accessToken1 = AccessToken("token123")
        val accessToken2 = AccessToken("token123")
        val accessToken3 = AccessToken("token456")
        
        assertEquals("Same access tokens should be equal", accessToken1, accessToken2)
        assertNotEquals("Different access tokens should not be equal", accessToken1, accessToken3)
        assertEquals("Same access tokens should have same hashCode", accessToken1.hashCode(), accessToken2.hashCode())
    }

    @Test
    fun accessToken_componentFunction_worksCorrectly() {
        val accessToken = AccessToken("testToken")
        
        val (token) = accessToken
        
        assertEquals("Component function should return token value", "testToken", token)
    }

    // RefreshToken DTO tests
    @Test
    fun refreshToken_constructor_setsPropertyCorrectly() {
        val tokenValue = "refresh_token_789012"
        
        val refreshToken = RefreshToken(tokenValue)
        
        assertEquals("Refresh token value should be set correctly", tokenValue, refreshToken.refreshToken)
    }

    @Test
    fun refreshToken_dataClass_behaviors_workCorrectly() {
        val refreshToken1 = RefreshToken("refresh123")
        val refreshToken2 = RefreshToken("refresh123")
        val refreshToken3 = RefreshToken("refresh456")
        
        assertEquals("Same refresh tokens should be equal", refreshToken1, refreshToken2)
        assertNotEquals("Different refresh tokens should not be equal", refreshToken1, refreshToken3)
        assertEquals("Same refresh tokens should have same hashCode", refreshToken1.hashCode(), refreshToken2.hashCode())
    }

    @Test
    fun refreshToken_componentFunction_worksCorrectly() {
        val refreshToken = RefreshToken("testRefreshToken")
        
        val (token) = refreshToken
        
        assertEquals("Component function should return token value", "testRefreshToken", token)
    }

    // Edge cases and boundary tests
    @Test
    fun dto_withEmptyStrings_handledCorrectly() {
        val emptyLogin = Login("", "")
        val emptyToken = Token("", "")
        val emptyAccessToken = AccessToken("")
        val emptyRefreshToken = RefreshToken("")
        
        assertEquals("Empty email should be handled", "", emptyLogin.email)
        assertEquals("Empty password should be handled", "", emptyLogin.password)
        assertEquals("Empty access token should be handled", "", emptyToken.accessToken)
        assertEquals("Empty refresh token should be handled", "", emptyToken.refreshToken)
        assertEquals("Empty access token value should be handled", "", emptyAccessToken.accessToken)
        assertEquals("Empty refresh token value should be handled", "", emptyRefreshToken.refreshToken)
    }

    @Test
    fun dto_withSpecialCharacters_handledCorrectly() {
        val specialEmail = "user+tag@example-domain.co.uk"
        val specialPassword = "P@ssw0rd!#$%^&*()"
        val specialToken = "token.with-special_chars123!@#"
        
        val login = Login(specialEmail, specialPassword)
        val token = Token(specialToken, specialToken)
        
        assertEquals("Special characters in email should be handled", specialEmail, login.email)
        assertEquals("Special characters in password should be handled", specialPassword, login.password)
        assertEquals("Special characters in tokens should be handled", specialToken, token.accessToken)
    }

    @Test
    fun dto_withUnicodeCharacters_handledCorrectly() {
        val unicodeEmail = "사용자@테스트.한국"
        val unicodePassword = "비밀번호123!"
        val unicodeToken = "토큰_123_값"
        
        val login = Login(unicodeEmail, unicodePassword)
        val token = Token(unicodeToken, unicodeToken)
        
        assertEquals("Unicode characters in email should be handled", unicodeEmail, login.email)
        assertEquals("Unicode characters in password should be handled", unicodePassword, login.password)
        assertEquals("Unicode characters in tokens should be handled", unicodeToken, token.accessToken)
    }

    @Test
    fun dto_withVeryLongStrings_handledCorrectly() {
        val longString = "a".repeat(1000)
        val longEmail = longString + "@example.com"
        val longPassword = longString + "123!"
        val longToken = "token_" + longString
        
        val login = Login(longEmail, longPassword)
        val token = Token(longToken, longToken)
        
        assertEquals("Very long email should be handled", longEmail, login.email)
        assertEquals("Very long password should be handled", longPassword, login.password)
        assertEquals("Very long token should be handled", longToken, token.accessToken)
        assertTrue("Long string should be properly stored", login.email.length > 1000)
    }

    @Test
    fun dto_nullSafety_stringProperties_neverNull() {
        // Data classes with non-nullable properties should never have null values
        val login = Login("test@example.com", "password")
        val token = Token("access", "refresh")
        val accessToken = AccessToken("access")
        val refreshToken = RefreshToken("refresh")
        
        assertNotNull("Login email should never be null", login.email)
        assertNotNull("Login password should never be null", login.password)
        assertNotNull("Token access token should never be null", token.accessToken)
        assertNotNull("Token refresh token should never be null", token.refreshToken)
        assertNotNull("AccessToken value should never be null", accessToken.accessToken)
        assertNotNull("RefreshToken value should never be null", refreshToken.refreshToken)
    }

    @Test
    fun dto_toString_containsAllFields() {
        val login = Login("user@test.com", "password123")
        val token = Token("accessToken", "refreshToken")
        
        val loginString = login.toString()
        val tokenString = token.toString()
        
        assertTrue("Login toString should contain email", loginString.contains("user@test.com"))
        assertTrue("Login toString should contain password reference", loginString.contains("password"))
        assertTrue("Token toString should contain access token", tokenString.contains("accessToken"))
        assertTrue("Token toString should contain refresh token", tokenString.contains("refreshToken"))
    }
}