package com.developeek.circleon.view.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.developeek.circleon.databinding.FragmentRegisterPostBinding

class RegisterPostFragment : Fragment() {
    private lateinit var binding: FragmentRegisterPostBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRegisterPostBinding.inflate(layoutInflater)

        return binding.root
    }
}
