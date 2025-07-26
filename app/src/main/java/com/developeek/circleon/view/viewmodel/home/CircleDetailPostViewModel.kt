package com.developeek.circleon.view.viewmodel.home

import android.os.Parcelable
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CircleDetailPostViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<CircleDetailPostScreen>
    val currentScrollState: Parcelable?
    val isLastPage: Boolean

    fun showLoadingAndRefresh()

    fun refresh()

    fun scrollOver()

    fun togglePin(post: PostModel)

    fun deleteAndFetch(postId: Int)

    fun requestReportPost(
        postId: Int,
        reportMessage: String,
    )

    fun saveScrollState(scrollState: Parcelable?)
}
