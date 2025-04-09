package com.developeek.circleon.view.viewmodel.circle

import androidx.lifecycle.LiveData
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CategoryModels
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState
import java.io.File
import java.time.LocalDateTime

interface UploadCircleViewModel {
    val state: LiveData<UiState>
    val circle: CircleDetailModel
    val categories: LiveData<CategoryModels>
    val error: String

    fun upload()

    fun edit()

    fun setCircleProfileImage(image: File?)

    fun setCircleIntroductionImage(image: File?)

    fun setCircleName(name: String)

    fun setRecruitmentStartDate(date: LocalDateTime)

    fun setRecruitmentEndDate(date: LocalDateTime)

    fun setSingleLineIntroduction(content: String)

    fun setIntroduction(content: String)

    fun setCategory(category: Category)

    fun removeCircleProfileImage()

    fun removeCircleIntroductionImage()
}
