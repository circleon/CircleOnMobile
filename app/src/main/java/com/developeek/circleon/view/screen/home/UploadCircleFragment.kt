package com.developeek.circleon.view.screen.home

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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UploadCircleFragment : Fragment() {
    private lateinit var binding: FragmentUploadCircleBinding
    private var editOrNot = false
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
            editOrNot =
                it.getBoolean(Const.FLAG_EDIT_OR_NOT).also { isEdit ->
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
    }
}
