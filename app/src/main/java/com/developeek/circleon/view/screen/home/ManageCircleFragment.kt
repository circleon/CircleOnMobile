package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentManageCircleBinding
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.viewmodel.ManageCircleViewModel
import com.developeek.circleon.view.viewmodelimpl.ManageCircleViewModelImpl
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.lifecycle.withCreationCallback

class ManageCircleFragment : Fragment() {
    private lateinit var binding: FragmentManageCircleBinding
    private lateinit var circle: CircleDetailModel
    private val viewModel: ManageCircleViewModel by viewModels<ManageCircleViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<ManageCircleViewModelImpl.ManageCircleViewModelFactory> {
                    it.create(circle)
                }
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circle = it.getSerializable(Const.TAG_CIRCLE_DETAIL) as CircleDetailModel
        }

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
        initCircleMemberView()
        hideBtmNav(parentActivity)
    }

    private fun initCircleMemberView() {
        binding.txtContentCircleMember.text =
            String.format(UNIT_CIRCLE_MEMBER, circle.members.size())
    }

    private fun hideBtmNav(activity: Activity) {
        activity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    companion object {
        private const val UNIT_CIRCLE_MEMBER = "멤버 %d명"
    }
}
