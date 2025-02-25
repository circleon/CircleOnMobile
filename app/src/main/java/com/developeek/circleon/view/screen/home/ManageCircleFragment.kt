package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentManageCircleBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManageCircleFragment : Fragment() {
    private lateinit var binding: FragmentManageCircleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentManageCircleBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireActivity())
    }

    private fun initView(parentActivity: Activity) {
        hideBtmNav(parentActivity)
    }

    private fun hideBtmNav(activity: Activity) {
        activity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }
}
