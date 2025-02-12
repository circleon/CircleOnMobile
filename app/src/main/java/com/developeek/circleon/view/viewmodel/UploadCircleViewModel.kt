package com.developeek.circleon.view.viewmodel

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.state.UiState
import java.io.File

interface UploadCircleViewModel {
    val state: LiveData<UiState>
    val error: String

    fun setCircleThumbnail(image: File?)

    fun setCircleIntroductionImage(image: File?)

    fun removeCircleThumbnail()

    fun removeCircleIntroductionImage()
}
