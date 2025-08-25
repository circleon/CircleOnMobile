package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.domain.model.PostEditResultModel
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.UploadPostScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface UploadPostViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<UploadPostScreen>
    val postEditResult: PostEditResultModel

    fun upload(content: String)

    fun edit(
        postId: Int,
        content: String,
    )

    fun setPostImage(image: File?)

    fun removePostImage()
}
