package com.developeek.circleon.view.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.databinding.FragmentCircleDetailNoticeBinding
import com.developeek.circleon.view.adapter.CircleNoticeAdapter

class CircleDetailNoticeFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailNoticeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailNoticeBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCircleNotice.adapter = CircleNoticeAdapter()
        binding.rvCircleNotice.layoutManager = LinearLayoutManager(requireActivity())
    }
}
