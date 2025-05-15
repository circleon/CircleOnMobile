package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.utils.Const
import java.io.Serializable

data class MemberModels(private val models: List<MemberModel>) : Serializable {
    fun get() = models

    fun size() = models.size

    fun isEmpty() = models.isEmpty()

    companion object {
        fun empty() = MemberModels(emptyList())
    }
}

data class MemberModel(
    val id: Int,
    val name: String,
    val status: MembershipStatus,
    val role: Role,
    val profileImgUrl: String?,
) : Serializable {
    // TODO: 멤버 조회 api 에 message 필드 nullable 로 추가될 경우 생성자 초기화 방식으로 수정 필요
    val message: String // 가입, 탈퇴 신청 메세지
        get() = _message
    private var _message = Const.EMPTY_TEXT

    fun setMessage(message: String) {
        _message = message
    }

    fun isSame(target: MemberModel) = this.id == target.id

    fun areContentsSame(target: MemberModel) = this == target
}
