package com.developeek.circleon.view.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.developeek.circleon.databinding.FragmentCircleDetailActivityPhotoBinding

class CircleDetailActivityPhotoFragment : Fragment() {
    private val binding: FragmentCircleDetailActivityPhotoBinding by lazy {
        FragmentCircleDetailActivityPhotoBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }
}
