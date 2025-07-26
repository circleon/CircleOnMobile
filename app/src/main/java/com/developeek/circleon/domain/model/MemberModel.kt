package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.utils.Const

data class MemberModel(
    val memberId: Int,
    val name: String,
    val status: MembershipStatus,
    val role: Role,
    val profileImgUrl: String?,
) : BaseModel(memberId) {
    // TODO: 멤버 조회 api 에 message 필드 nullable 로 추가될 경우 생성자 초기화 방식으로 수정 필요
    val message: String // 가입, 탈퇴 신청 메세지
        get() = _message
    private var _message = Const.EMPTY_TEXT

    fun setMessage(message: String) {
        _message = message
    }

    override fun areContentsSame(target: BaseModel): Boolean {
        if (target !is MemberModel) return false

        return target == this
    }
}
