package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.state.UiState

interface CircleDetailPostDetailViewModel {
    val state: LiveData<UiState>
    val comments: CommentModels
    val error: String

    fun refresh()

    /**
     * updateUiState()
     *
     * post 상세 화면 진입 anim 이 종료된 이후 ui 를 업데이트 하기 위한 uiState 업데이트 전용 함수
     * 데이터 로딩 작업은 기존과 같이 뷰모델 init 에서 수행
     */
    fun updateUiState()
}
