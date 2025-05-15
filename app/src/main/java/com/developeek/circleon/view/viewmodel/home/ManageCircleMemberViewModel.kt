package com.developeek.circleon.view.viewmodel.home

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.state.UiState

interface ManageCircleMemberViewModel {
    val state: LiveData<UiState>
    val members: MemberModels
    val error: String

    fun editCircleMemberRole(
        member: MemberModel,
        role: Role,
    )

    fun banCircleMember(member: MemberModel)

    fun acceptJoinRequest(member: MemberModel)

    fun rejectJoinRequest(member: MemberModel)

    fun acceptLeaveRequest(member: MemberModel)
}
