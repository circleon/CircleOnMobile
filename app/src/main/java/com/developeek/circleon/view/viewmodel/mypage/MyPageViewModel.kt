package com.developeek.circleon.view.viewmodel.mypage

import com.developeek.circleon.view.Event
import kotlinx.coroutines.flow.SharedFlow
import java.io.File

interface MyPageViewModel {
    val event: SharedFlow<Event>

    fun setUserProfileImage(image: File?)

    fun logout()
}
