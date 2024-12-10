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

    fun isSame(role: Role) = this.code == role.code

    fun codeName() = code

    fun roleName() = roleName

    companion object {
        fun findOrNull(code: String?) = Role.entries.find { it.code == code }
    }
}
