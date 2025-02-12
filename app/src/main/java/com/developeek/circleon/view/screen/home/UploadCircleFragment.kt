package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentUploadCircleBinding
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils.toJPEG
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.viewmodel.UploadCircleViewModel
import com.developeek.circleon.view.viewmodelimpl.UploadCircleViewModelImpl
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UploadCircleFragment : Fragment() {
    private lateinit var binding: FragmentUploadCircleBinding
    private var isEdit = false
    private lateinit var circle: CircleDetailModel
    private val viewModel: UploadCircleViewModel by viewModels<UploadCircleViewModelImpl>()
    private val circleThumbnailPickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                uri ->
            uri?.let {
                glideProvider.loadImage(it, requireActivity(), binding.btnAddCircleThumbnail)
                viewModel.setCircleThumbnail(it.toJPEG(requireActivity()))
                binding.btnRemoveCircleThumbnail.isVisible = true
                binding.btnAddCircleThumbnail.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.bg_circle,
                    ),
                )
            }
        }
    private val circleIntroductionImagePickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                uri ->
            uri?.let {
                glideProvider.loadImage(it, requireActivity(), binding.btnAddCircleIntroductionImage)
                viewModel.setCircleIntroductionImage(it.toJPEG(requireActivity()))
                binding.btnRemoveCircleIntroductionImage.isVisible = true
                binding.btnAddCircleIntroductionImage.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.bg_rounded_rectangle,
                    ),
                )
            }
        }

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isEdit =
                it.getBoolean(Const.FLAG_IS_EDIT).also { isEdit ->
                    if (isEdit) {
                        circle = it.getSerializable(Const.TAG_CIRCLE_DETAIL) as CircleDetailModel
                    }
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUploadCircleBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireActivity())
        initListener(requireActivity())
    }

    private fun initView(activity: Activity) {
        hideBtmNav(activity)

        if (isEdit) {
            loadCircleContentWhenIsEdit(activity)
        }
    }

    private fun loadCircleContentWhenIsEdit(activity: Activity) {
        if (isEdit) {
            binding.edtCircleName.setText(circle.name)
            binding.edtCircleSingleLineIntroduction.setText(circle.comment)
            binding.edtCircleIntroduction.setText(circle.introduction)
            loadCircleThumbnailWhenIsNotNull(activity)
            loadCircleIntroductionImageWhenIsNotNull(activity)
        }
    }

    private fun loadCircleThumbnailWhenIsNotNull(activity: Activity) {
        circle.thumbnailUrl?.let {
            glideProvider.fetchImage(
                it,
                activity,
                binding.btnAddCircleThumbnail,
            )
            binding.btnAddCircleThumbnail.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    activity,
                    R.drawable.bg_circle,
                ),
            )
            binding.btnRemoveCircleThumbnail.isVisible = true
        }
    }

    private fun loadCircleIntroductionImageWhenIsNotNull(activity: Activity) {
        circle.introImgUrl?.let {
            glideProvider.fetchImage(
                it,
                activity,
                binding.btnAddCircleIntroductionImage,
            )
            binding.btnAddCircleIntroductionImage.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    activity,
                    R.drawable.bg_rounded_rectangle,
                ),
            )
            binding.btnRemoveCircleIntroductionImage.isVisible = true
        }
    }

    private fun hideBtmNav(activity: Activity) {
        activity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    private fun initListener(activity: Activity) {
        setBtnAddCircleThumbnailListener()
        setBtnAddCircleIntroductionImageListener()
        setBtnRemoveCircleThumbnailListener(activity)
        setBtnRemoveCircleIntroductionImageListener(activity)
    }

    private fun setBtnAddCircleThumbnailListener() {
        binding.btnAddCircleThumbnail.setOnClickListener {
            val mimeType = "image/jpeg"
            circleThumbnailPickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType)),
            )
        }
    }

    private fun setBtnAddCircleIntroductionImageListener() {
        binding.btnAddCircleIntroductionImage.setOnClickListener {
            val mimeType = "image/jpeg"
            circleIntroductionImagePickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType)),
            )
        }
    }

    private fun setBtnRemoveCircleThumbnailListener(activity: Activity) {
        binding.btnRemoveCircleThumbnail.setOnClickListener {
            viewModel.removeCircleThumbnail()
            binding.btnAddCircleThumbnail.setImageResource(R.drawable.ic_add)
            binding.btnRemoveCircleThumbnail.isVisible = false
            binding.btnAddCircleThumbnail.setBackgroundDrawable(
                ContextCompat.getDrawable(activity, R.drawable.bg_dotted_circle),
            )
        }
    }

    private fun setBtnRemoveCircleIntroductionImageListener(activity: Activity) {
        binding.btnRemoveCircleIntroductionImage.setOnClickListener {
            viewModel.removeCircleIntroductionImage()
            binding.btnAddCircleIntroductionImage.setImageResource(R.drawable.ic_add)
            binding.btnRemoveCircleIntroductionImage.isVisible = false
            binding.btnAddCircleIntroductionImage.setBackgroundDrawable(
                ContextCompat.getDrawable(activity, R.drawable.bg_dotted_rounded_rectangle),
            )
        }
    }
}
