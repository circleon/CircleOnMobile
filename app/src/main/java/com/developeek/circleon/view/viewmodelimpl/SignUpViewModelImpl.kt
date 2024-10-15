package com.developeek.circleon.view.viewmodelimpl

import androidx.lifecycle.ViewModel
import com.developeek.circleon.data.repository.LoginRepository
import com.developeek.circleon.view.viewmodel.SignUpViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModelImpl
    @Inject
    constructor(private val repository: LoginRepository) : SignUpViewModel, ViewModel()
