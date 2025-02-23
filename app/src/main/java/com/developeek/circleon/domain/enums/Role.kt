package com.developeek.circleon.domain.enums

enum class Role(
    private val code: String,
    private val roleName: String,
) {
    NONE("NONE", ""),
    PRESIDENT("PRESIDENT", "회장"),
    EXECUTIVE("EXECUTIVE", "임원"),
    MEMBER("MEMBER", "부원"),
    ;

    fun isMember() = this.code != NONE.code

    fun isExecutive() = this.code == EXECUTIVE.code || this.code == PRESIDENT.code

    fun codeName() = code

    fun roleName() = roleName

    companion object {
        private val default = NONE

        fun findOrDefault(code: String?) = entries.find { it.code == code } ?: default
    }
}
