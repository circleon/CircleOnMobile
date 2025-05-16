package com.developeek.circleon.view.screen.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.developeek.circleon.databinding.FragmentMyPostBinding

class MyPostFragment : Fragment() {
    private lateinit var binding: FragmentMyPostBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyPostBinding.inflate(layoutInflater)

        return binding.root
    }
}
