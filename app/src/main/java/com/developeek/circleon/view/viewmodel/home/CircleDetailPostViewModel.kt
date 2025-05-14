package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CircleDetailPostViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<CircleDetailPostScreen>
    val isLastPage: Boolean

    fun refresh()

    fun scrollOver()

    fun pinAndFetch(postId: Int)

    fun removePinAndFetch(postId: Int)

    fun deleteAndFetch(postId: Int)

    fun requestReportPost(
        postId: Int,
        reportMessage: String,
    )
}
