package com.developeek.circleon.domain.enums

enum class Role(
    private val codeName: String,
    private val roleName: String,
) {
    NONE_MEMBER("NONE_MEMBER", "비회원"),
    MEMBER("MEMBER", "부원"),
    EXECUTIVE("EXECUTIVE", "임원"),
    PRESIDENT("PRESIDENT", "회장"),
    ;

    fun isMember() = this != NONE_MEMBER

    fun isExecutive() = this == EXECUTIVE || this == PRESIDENT

    fun codeName() = codeName

    fun roleName() = roleName

    companion object {
        private val default = NONE_MEMBER

        fun findOrDefault(code: String?) = entries.find { it.codeName == code } ?: default
    }
}
