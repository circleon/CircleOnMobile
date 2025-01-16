package com.developeek.circleon.view.viewmodel

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.state.UiState

interface CircleDetailPostDetailViewModel {
    val state: LiveData<UiState>
    val deletePostState: LiveData<Boolean>
    val comments: CommentModels
    val scrollOver: LiveData<Boolean>
    val scrollListener: RecyclerView.OnScrollListener
    val currentScrollState: Parcelable?
    val error: String

    fun refresh()

    fun notifyEnterAnimFinishedAndUpdateUI()

    fun scrollOver()

    fun saveScrollState(scrollState: Parcelable?)

    fun registerComment(comment: String)

    fun delete()

    fun deleteComment(commentId: Int)
}
