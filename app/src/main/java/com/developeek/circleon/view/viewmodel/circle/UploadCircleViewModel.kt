package com.developeek.circleon.view.viewmodel.circle

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.circle.UploadCircleScreen
import com.developeek.circleon.view.viewmodelimpl.circle.UploadCircleScreenEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.time.LocalDateTime

interface UploadCircleViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<UploadCircleScreen>
    val uploadCircleScreenEvent: SharedFlow<UploadCircleScreenEvent>
    val circle: CircleDetailModel

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

    fun toggleRecruitmentLock(isRecruiting: Boolean)

    fun removeCircleProfileImage()

    fun removeCircleIntroductionImage()
}
