package com.developeek.circleon.domain.enums

enum class University(
    private val codeName: String,
    private val univName: String,
) {
    AJOU("AJOU", "아주대학교"), ;

    fun univName() = univName

    companion object {
        fun findOrNull(code: String) = entries.find { it.codeName == code }
    }
}
