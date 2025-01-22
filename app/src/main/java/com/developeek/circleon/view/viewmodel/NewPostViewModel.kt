package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.state.UiState
import java.io.File

interface NewPostViewModel {
    val state: LiveData<UiState>
    val error: String

    fun upload(content: String)

    fun setPostImage(image: File?)

    fun removePostImage()
}
