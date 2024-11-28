package com.developeek.circleon.view.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.developeek.circleon.databinding.FragmentCircleDetailAnnouncementBinding

class CircleDetailAnnouncementFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailAnnouncementBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailAnnouncementBinding.inflate(layoutInflater)

        return binding.root
    }
}
