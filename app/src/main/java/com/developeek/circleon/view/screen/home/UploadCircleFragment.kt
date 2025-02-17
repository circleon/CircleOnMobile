package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentUploadCircleBinding
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils.toJPEG
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.UploadCircleViewModel
import com.developeek.circleon.view.viewmodelimpl.UploadCircleViewModelImpl
import com.developeek.circleon.view.widget.ErrorAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
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
                binding.btnAddCircleThumbnail.background = null
            }
        }
    private val circleIntroductionImagePickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                uri ->
            uri?.let {
                glideProvider.loadImage(it, requireActivity(), binding.btnAddCircleIntroductionImage)
                viewModel.setCircleIntroductionImage(it.toJPEG(requireActivity()))
                binding.btnRemoveCircleIntroductionImage.isVisible = true
                binding.btnAddCircleIntroductionImage.background = null
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
                        viewModel.origin(circle)
                    }
                }
        }
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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
        initObserver(requireActivity())
        initListener(requireActivity())
    }

    private fun initView(activity: Activity) {
        hideBtmNav(activity)

        if (isEdit) {
            loadOriginalContentWhenIsEdit(activity)
        }
    }

    private fun loadOriginalContentWhenIsEdit(activity: Activity) {
        if (isEdit) {
            binding.edtCircleName.setText(viewModel.origin.name)
            viewModel.origin.recruitmentStartDate?.let {
                binding.txtRecruitmentStartDate.text = it.format(DateTimeFormatter.ofPattern(RECRUITMENT_DATE_FORMAT))
            }
            viewModel.origin.recruitmentEndDate?.let {
                binding.txtRecruitmentEndDate.text = it.format(DateTimeFormatter.ofPattern(RECRUITMENT_DATE_FORMAT))
            }
            binding.edtCircleSingleLineIntroduction.setText(viewModel.origin.singleLineIntroduction)
            binding.txtCurrentCircleSingleIntroductionSize.text =
                viewModel.origin.singleLineIntroduction.length.toString()
            binding.edtCircleIntroduction.setText(viewModel.origin.introduction)
            loadOriginalThumbnailWhenIsNotNull(activity)
            loadOriginalIntroductionImageWhenIsNotNull(activity)
        }
    }

    private fun loadOriginalThumbnailWhenIsNotNull(activity: Activity) {
        viewModel.origin.thumbnailUrl?.let {
            glideProvider.fetchImage(
                it,
                activity,
                binding.btnAddCircleThumbnail,
            )
            binding.btnAddCircleThumbnail.background = null
            binding.btnRemoveCircleThumbnail.isVisible = true
        }
    }

    private fun loadOriginalIntroductionImageWhenIsNotNull(activity: Activity) {
        viewModel.origin.introImgUrl?.let {
            glideProvider.fetchImage(
                it,
                activity,
                binding.btnAddCircleIntroductionImage,
            )
            binding.btnAddCircleIntroductionImage.background = null
            binding.btnRemoveCircleIntroductionImage.isVisible = true
        }
    }

    private fun hideBtmNav(activity: Activity) {
        activity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    private fun initObserver(activity: Activity) {
        viewModel.state.observe(
            viewLifecycleOwner,
            stateObserver(activity),
        )
    }

    private fun stateObserver(activity: Activity) =
        Observer<UiState> {
            val loadingIndicator = activity.findViewById<CircularProgressIndicator>(R.id.pgbLoading)
            loadingIndicator.isVisible = it is UiState.Loading
            when (it) {
                UiState.Success -> {
                    sendUserToPreviousScreen()
                }
                UiState.AuthenticationError -> {
                    sendUserToLoginScreen(activity)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
                UiState.ServiceError -> {
                    ErrorAlertDialog(activity, viewModel.error).show()
                }
                else -> {}
            }
        }

    private fun initListener(activity: Activity) {
        setBtnCancelListener()
        setBtnUploadListener()
        setBtnAddCircleThumbnailListener()
        setBtnAddCircleIntroductionImageListener()
        setBtnEdtCircleNameListener()
        setBtnEdtSingleLineIntroductionListener()
        setBtnEdtCircleIntroductionListener()
        setBtnRemoveCircleThumbnailListener(activity)
        setBtnRemoveCircleIntroductionImageListener(activity)
        setEdtSingleLineIntroductionListener()
    }

    private fun setBtnCancelListener() {
        binding.btnCancel.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun setBtnUploadListener() {
        binding.btnUpload.setOnClickListener {
            if (isEdit) viewModel.edit()
        }
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

    private fun setBtnEdtCircleNameListener() {
        binding.edtCircleName.doAfterTextChanged {
            it?.let {
                viewModel.setCircleName(it.toString())
            }
        }
    }

    private fun setBtnEdtSingleLineIntroductionListener() {
        binding.edtCircleSingleLineIntroduction.doAfterTextChanged {
            it?.let {
                viewModel.setSingleLineIntroduction(it.toString())
            }
        }
    }

    private fun setBtnEdtCircleIntroductionListener() {
        binding.edtCircleIntroduction.doAfterTextChanged {
            it?.let {
                viewModel.setIntroduction(it.toString())
            }
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

    private fun setEdtSingleLineIntroductionListener() {
        binding.edtCircleSingleLineIntroduction.doAfterTextChanged {
            if (it == null) {
                binding.txtCurrentCircleSingleIntroductionSize.text = "0"
            } else {
                binding.txtCurrentCircleSingleIntroductionSize.text = it.length.toString()
            }
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().navigateUp()
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    companion object {
        private const val RECRUITMENT_DATE_FORMAT = "yyyy.MM.dd"
    }
}
