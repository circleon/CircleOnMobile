package com.developeek.circleon.view.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentCircleDetailBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CircleDetailFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailBinding
    private lateinit var fragmentManager: FragmentManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailBinding.inflate(layoutInflater)
        fragmentManager = requireActivity().supportFragmentManager

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListener()
//        arguments?.let {
//            binding.txtCircleId.text = String.format("circleId: %s", it.getInt(Const.TAG_CIRCLE_ID).toString())
//        }
    }

    private fun initView() {
        initToolbar()
        initBottomNav()
    }

    private fun initToolbar() {
        binding.tbCircleDetail.inflateMenu(R.menu.menu_circle_settings)
    }

    private fun initBottomNav() {
        add(CircleDetailIntroductionFragment())
    }

    private fun initListener() {
        setBtnBackListener()
        setTlCircleDetailListener()
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setTlCircleDetailListener() {
        binding.tlCircleDetail.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.position) {
                        0 -> {
                            replaceTo(CircleDetailIntroductionFragment())
                        }
                        1 -> {
                            replaceTo(CircleDetailNoticeFragment())
                        }
                        2 -> {
                            replaceTo(CircleDetailPostFragment())
                        }
                        3 -> {
                            replaceTo(CircleDetailActivityPhotoFragment())
                        }
                    }
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabReselected(p0: TabLayout.Tab?) {
                }
            },
        )
    }

    private fun add(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.add(binding.flCircleDetail.id, fragment)
        transaction.commit()
    }

    private fun replaceTo(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(binding.flCircleDetail.id, fragment)
        transaction.commit()
    }
}
