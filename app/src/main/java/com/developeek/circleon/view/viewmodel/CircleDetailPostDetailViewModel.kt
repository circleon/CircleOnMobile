package com.developeek.circleon.view.viewmodel

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.model.Identifiable
import com.developeek.circleon.domain.state.UiState

interface CircleDetailPostDetailViewModel {
    val state: LiveData<UiState>
    val uploadCommentState: LiveData<UiState>
    val editCommentState: LiveData<UiState>
    val deleteCommentState: LiveData<UiState>
    val deletePostState: LiveData<UiState>
    val contents: List<Identifiable>
    val comments: CommentModels
    val scrollOver: LiveData<Boolean>
    val scrollListener: RecyclerView.OnScrollListener
    val currentScrollState: Parcelable?
    val error: String

    fun refresh()

    fun scrollOver()

    fun saveScrollState(scrollState: Parcelable?)

    fun uploadComment(content: String)

    fun editComment(
        commentId: Int,
        content: String,
    )

    fun deleteComment(commentId: Int)

    fun delete()
}
