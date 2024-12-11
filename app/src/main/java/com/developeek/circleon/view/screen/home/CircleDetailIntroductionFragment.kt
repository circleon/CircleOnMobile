package com.developeek.circleon.view.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.developeek.circleon.databinding.FragmentCircleDetailIntroductionBinding
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.utils.glide.GlideProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailIntroductionFragment(
    private val circleDetail: CircleDetailModel,
) : Fragment() {
    private lateinit var binding: FragmentCircleDetailIntroductionBinding

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailIntroductionBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        binding.txtCircleIntroductionContent.text = circleDetail.introduction
        if (circleDetail.introImgUrl == null) {
            binding.imgCircleIntroduction.isVisible = false
        } else {
            glideProvider.callImage(circleDetail.introImgUrl, requireActivity(), binding.imgCircleIntroduction)
        }
    }
}
