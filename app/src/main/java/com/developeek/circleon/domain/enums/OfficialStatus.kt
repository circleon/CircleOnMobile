package com.developeek.circleon.domain.enums

enum class OfficialStatus(
    private val codeName: String,
) {
    OFFICIAL("OFFICIAL"),
    OFFICIAL_REQUESTED("REQUEST"),
    UNOFFICIAL("UNOFFICIAL"),
    ;

    fun codeName() = codeName

    fun isOfficial() = this == OFFICIAL

    companion object {
        fun find(code: String) = entries.find { it.codeName == code } ?: UNOFFICIAL
    }
}
