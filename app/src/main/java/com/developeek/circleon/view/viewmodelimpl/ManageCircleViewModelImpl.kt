package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.viewmodel.ManageCircleViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ManageCircleViewModelImpl.ManageCircleViewModelFactory::class)
class ManageCircleViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circle") private val circle: CircleDetailModel,
        private val repository: CircleRepository,
    ) : ManageCircleViewModel, ViewModel() {
        @AssistedFactory interface ManageCircleViewModelFactory {
            fun create(
                @Assisted("circle") circle: CircleDetailModel,
            ): ManageCircleViewModelImpl
        }

        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        override lateinit var circleMembers: MemberModels
        override lateinit var joinRequestedMembers: MemberModels
        override lateinit var leaveRequestedMembers: MemberModels
        private var fetchMembersJob: Job? = null
        private var currentPage = DEFAULT_PAGE

        init {
            fetchMembers(currentPage, SIZE_BY_PAGE)
        }

        private fun fetchMembers(
            page: Int,
            size: Int,
        ) {
            fetchMembersJob?.let {
                if (!it.isCompleted) return
            }

            uiState.postValue(UiState.Loading)

            fetchMembersJob =
                viewModelScope.launch {
                }
        }

        companion object {
            private const val SIZE_BY_PAGE = 200 // 멤버 데이터 일괄 호출
            private const val DEFAULT_PAGE = 0
        }
    }
