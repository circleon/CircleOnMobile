package com.developeek.circleon.view.viewmodel.home

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.state.UiState
import java.io.File

interface UploadPostViewModel {
    val state: LiveData<UiState>
    val error: String

    fun upload(content: String)

    fun edit(
        postId: Int,
        content: String,
    )

    fun setPostImage(image: File?)

    fun removePostImage()
}
