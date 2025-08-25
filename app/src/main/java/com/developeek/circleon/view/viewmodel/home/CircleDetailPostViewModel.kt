package com.developeek.circleon.view.viewmodel.home

import android.os.Parcelable
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.model.UserModel
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CircleDetailPostViewModel {
    val user: UserModel
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<CircleDetailPostScreen>
    val currentScrollState: Parcelable?
    val isLastPage: Boolean

    fun showLoadingAndRefresh()

    fun refresh()

    fun scrollOver()

    fun togglePin(post: PostModel)

    // 네트워크 통신 없이 뷰모델 내부 데이터에서만 진행
    fun updatePostItem(
        post: PostModel,
        content: String,
    )

    fun delete(post: PostModel)

    // 네트워크 통신 없이 뷰모델 내부 데이터에서만 진행
    fun deletePostItem(post: PostModel)

    fun requestReportPost(
        postId: Int,
        reportMessage: String,
    )

    fun saveScrollState(scrollState: Parcelable?)
}
