package com.developeek.circleon.domain.enums

enum class MembershipStatus(
    private val codeName: String,
    private val statusName: String,
) {
    NOT_JOINED("NOT_JOINED", "미가입"),
    JOINED("APPROVED", "가입"),
    JOIN_REQUESTED("PENDING", "가입 요청 중"),
    JOIN_REJECTED("REJECTED", "가입 거절"),
    LEAVE_REQUESTED("LEAVE_REQUEST", "탈퇴 요청 중"),
    ;

    fun codeName() = codeName

    fun statusName() = statusName

    companion object {
        private val default = NOT_JOINED

        fun findOrDefault(status: String) = entries.find { it.codeName == status } ?: default
    }
}
