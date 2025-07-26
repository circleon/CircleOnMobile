package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostDetailScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CircleDetailPostDetailViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<CircleDetailPostDetailScreen>
    val isLastPage: Boolean

    fun showLoadingAndRefresh()

    fun scrollOver()

    fun uploadComment(content: String)

    fun editComment(
        commentId: Int,
        content: String,
    )

    fun deleteComment(commentId: Int)

    fun delete()

    fun reportPost(message: String)

    fun reportComment(
        commentId: Int,
        message: String,
    )
}
