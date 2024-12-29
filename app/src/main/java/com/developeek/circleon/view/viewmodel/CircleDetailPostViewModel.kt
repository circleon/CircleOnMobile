package com.developeek.circleon.view.viewmodel

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.domain.model.PostModels
import com.developeek.circleon.domain.state.UiState

interface CircleDetailPostViewModel {
    val state: LiveData<UiState>
    val posts: PostModels
    val scrollOver: LiveData<Boolean>
    val scrollListener: RecyclerView.OnScrollListener
    val currentScrollState: Parcelable?
    val currentTopOrNot: Boolean
    val error: String

    fun refresh()

    fun scrollOver(circleId: Int)

    fun saveScrollState(scrollState: Parcelable?)

    fun removeScrollState()

    fun setTopOrNot(isTop: Boolean)

    fun pinAndLoad(postId: Int)

    fun removePinAndLoad(postId: Int)
}
