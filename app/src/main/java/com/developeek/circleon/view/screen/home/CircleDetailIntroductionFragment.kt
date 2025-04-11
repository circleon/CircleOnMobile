package com.developeek.circleon.view.screen.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.developeek.circleon.databinding.FragmentCircleDetailIntroductionBinding
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailIntroductionFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailIntroductionBinding
    private lateinit var circleDetail: CircleDetailModel

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circleDetail = it.getSerializable(Const.TAG_CIRCLE_DETAIL) as CircleDetailModel
        }
    }

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

        initView(requireContext())
    }

    private fun initView(context: Context) {
        loadIntroductionContent(context)
        loadRecruitmentDate()
    }

    private fun loadIntroductionContent(context: Context) {
        if (circleDetail.introduction == null && circleDetail.introImgUrl == null) {
            binding.llCircleIntroduction.isVisible = false
            return
        }

        if (circleDetail.introduction == null) {
            binding.txtCircleIntroductionContent.isVisible = false
        } else {
            binding.txtCircleIntroductionContent.text = circleDetail.introduction
        }
        if (circleDetail.introImgUrl == null) {
            binding.cvCircleIntroduction.isVisible = false
        } else {
            glideProvider.fetchImage(circleDetail.introImgUrl!!, context, binding.imgCircleIntroduction)
        }
    }

    private fun loadRecruitmentDate() {
        if (circleDetail.recruitmentStartDate == null || circleDetail.recruitmentEndDate == null) {
            binding.txtRecruitmentDate.text = NO_RECRUITMENT_MESSAGE
        } else {
            val start = circleDetail.recruitmentStartDate!!.format(DateTimeFormatter.ofPattern(RECRUITMENT_DATE_FORMAT))
            val startDayOfWeek =
                String.format(
                    DAY_OF_WEEK_UNIT,
                    circleDetail.recruitmentStartDate!!.dayOfWeek
                        .getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                )
            val end = circleDetail.recruitmentEndDate!!.format(DateTimeFormatter.ofPattern(RECRUITMENT_DATE_FORMAT))
            val endDayOfWeek =
                String.format(
                    DAY_OF_WEEK_UNIT,
                    circleDetail.recruitmentEndDate!!.dayOfWeek
                        .getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                )
            binding.txtRecruitmentDate.text = start + startDayOfWeek + RECRUITMENT_DATE_DIVIDER + end + endDayOfWeek
        }
    }

    companion object {
        private const val NO_RECRUITMENT_MESSAGE = "예정 없음"
        private const val RECRUITMENT_DATE_FORMAT = "M월 d일"
        private const val DAY_OF_WEEK_UNIT = "(%s)"
        private const val RECRUITMENT_DATE_DIVIDER = " ~ "
    }
}
