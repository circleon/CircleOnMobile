package com.developeek.circleon.domain.enums

enum class University(
    private val code: String,
    private val univName: String,
) {
    AJOU("AJOU", "아주대학교"), ;

    fun univName() = univName

    companion object {
        fun findOrNull(code: String) = University.entries.find { it.code == code }
    }
}
