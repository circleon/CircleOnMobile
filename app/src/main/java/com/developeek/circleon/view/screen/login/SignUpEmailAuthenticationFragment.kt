package com.developeek.circleon.view.screen.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.developeek.circleon.databinding.FragmentSignUpEmailAuthenticationBinding
import com.developeek.circleon.view.viewmodelimpl.SignUpViewModelImpl

class SignUpEmailAuthenticationFragment : Fragment() {
    private lateinit var binding: FragmentSignUpEmailAuthenticationBinding
    private val viewModel: SignUpViewModelImpl by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUpEmailAuthenticationBinding.inflate(layoutInflater)
        return binding.root
    }
}
