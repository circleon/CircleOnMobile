package com.developeek.circleon.domain.enums

enum class Role(
    val id: Int,
    val codeName: String,
    val roleName: String,
) {
    NONE_MEMBER(0, "NONE_MEMBER", "비회원"),
    MEMBER(1, "MEMBER", "부원"),
    EXECUTIVE(2, "EXECUTIVE", "임원"),
    PRESIDENT(3, "PRESIDENT", "회장"),
    ;

    fun isMember() = this != NONE_MEMBER

    fun isExecutive() = this == EXECUTIVE || this == PRESIDENT

    fun isPresident() = this == PRESIDENT

    companion object {
        private val default = NONE_MEMBER

        fun findOrDefault(code: String?) = entries.find { it.codeName == code } ?: default

        fun getCircleRoles() = entries.filter { it != NONE_MEMBER }

        fun indexOf(role: Role) = entries.indexOf(role)
    }
}
