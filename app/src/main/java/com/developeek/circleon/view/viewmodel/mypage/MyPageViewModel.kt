package com.developeek.circleon.view.viewmodel.mypage

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.mypage.MyPageScreenEvent
import kotlinx.coroutines.flow.SharedFlow
import java.io.File

interface MyPageViewModel {
    val event: SharedFlow<Event>
    val myPageScreenEvent: SharedFlow<MyPageScreenEvent>

    fun setUserProfileImage(image: File?)

    fun removeUserProfileImage()

    fun logout()
}
