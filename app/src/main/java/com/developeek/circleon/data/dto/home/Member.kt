package com.developeek.circleon.data.dto.home

import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.MemberModel
import com.google.gson.annotations.SerializedName

/**
 * Member
 *
 * 특정 동아리 가입 상태와 상관없이 모든 사용자를 다루기 위한 DTO
 * 동아리 멤버, 가입 신청, 탈퇴 신청 등 인원 관리 작업에 사용된다.
 */
data class Member(
    @SerializedName("memberId") val id: Int,
    @SerializedName("memberName") val name: String,
    @SerializedName("membershipStatus") val status: String,
    @SerializedName("circleRole") val role: String?,
    // TODO: 회원 수정 기능 제작 이후 프로필 이미지 추가 및 프로필 이미지 전용 url 변환 Utils 에 추가
    @SerializedName("memberProfileUrl") val profileImgUrl: String?,
    val joinedAt: String?,
) {
    fun toMemberModel() =
        MemberModel(
            id,
            name,
            MembershipStatus.findOrDefault(status),
            Role.findOrDefault(role),
            profileImgUrl,
        )
}
