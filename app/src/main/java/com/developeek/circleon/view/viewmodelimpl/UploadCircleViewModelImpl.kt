package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.viewmodel.UploadCircleViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadCircleViewModelImpl
    @Inject
    constructor(
        private val repository: CircleRepository,
    ) : UploadCircleViewModel, ViewModel() {
        override val state: LiveData<UiState>
            get() = uiState
        private val uiState = MutableLiveData<UiState>()

        private var thumbnail: File? = null
        private var introductionImage: File? = null

        override lateinit var error: String

        override fun setCircleThumbnail(image: File?) {
            this.thumbnail = image
        }

        override fun setCircleIntroductionImage(image: File?) {
            this.introductionImage = image
        }
    }
