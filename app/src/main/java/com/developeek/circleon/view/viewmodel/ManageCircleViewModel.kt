package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.state.UiState

interface ManageCircleViewModel {
    val state: LiveData<UiState>
    val circleMembers: MemberModels
    val joinRequestedMembers: MemberModels
    val leaveRequestedMembers: MemberModels
    val error: String
}
