package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.state.UiState

interface CircleDetailPostDetailViewModel {
    val state: LiveData<UiState>
    val registerCommentState: LiveData<Boolean>
    val deletePostState: LiveData<Boolean>
    val deleteCommentState: LiveData<Boolean>
    val comments: CommentModels
    val error: String

    fun refresh()

    fun notifyEnterAnimFinishedAndUpdateUI()

    fun registerComment(comment: String)

    fun delete()

    fun deleteComment(comment: CommentModel)
}
