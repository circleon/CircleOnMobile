package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.ManageCircleMemberScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ManageCircleMemberViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<ManageCircleMemberScreen>

    fun editCircleMemberRole(
        member: MemberModel,
        role: Role,
    )

    fun banCircleMember(member: MemberModel)

    fun acceptJoinRequest(member: MemberModel)

    fun rejectJoinRequest(member: MemberModel)

    fun acceptLeaveRequest(member: MemberModel)
}
