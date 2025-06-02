package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.UploadPostScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface UploadPostViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<UploadPostScreen>

    fun upload(content: String)

    fun edit(
        postId: Int,
        content: String,
    )

    fun setPostImage(image: File?)

    fun removePostImage()
}
