package com.developeek.circleon.view.viewmodel.mypage

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.mypage.MyPostScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MyPostViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<MyPostScreen>
    val isLastPage: Boolean

    fun refresh()

    fun scrollOver()
}
