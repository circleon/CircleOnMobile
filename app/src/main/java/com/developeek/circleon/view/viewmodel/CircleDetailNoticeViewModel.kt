package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.domain.model.PostModels
import com.developeek.circleon.domain.state.UiState

interface CircleDetailNoticeViewModel {
    val state: LiveData<UiState>
    val notices: PostModels
    val scrollOver: LiveData<Boolean>
    val scrollListener: RecyclerView.OnScrollListener
    val error: String

    fun load(circleId: Int)

    fun scrollOver(circleId: Int)
}
