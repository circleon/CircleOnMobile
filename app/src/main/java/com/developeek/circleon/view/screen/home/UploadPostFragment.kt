package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Context
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentUploadPostBinding
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils.toJPEG
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.screen.base.BaseFragment
import com.developeek.circleon.view.viewmodel.home.UploadPostViewModel
import com.developeek.circleon.view.viewmodelimpl.home.UploadPostScreen
import com.developeek.circleon.view.viewmodelimpl.home.UploadPostViewModelImpl
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UploadPostFragment : BaseFragment() {
    private val binding: FragmentUploadPostBinding by lazy {
        FragmentUploadPostBinding.inflate(layoutInflater)
    }
    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                uri ->
            uri?.let {
                glideProvider.loadImage(it, requireContext(), binding.btnAddPostImage)
                viewModel.setPostImage(it.toJPEG(requireContext()))
                binding.btnRemovePostImage.isVisible = true
                binding.txtAddPostImage.isVisible = false
                binding.btnAddPostImage.background = null
            }
        }
    private val viewModel: UploadPostViewModel by viewModels<UploadPostViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<UploadPostViewModelImpl.UploadPostViewModelFactory> {
                    it.create(circleId, postType)
                }
        },
    )
    private lateinit var post: PostModel
    private lateinit var postType: PostType // 편집이 아닌 상황에서는 item 전달이 되지 않기 때문에 post type 을 별도로 수신
    private var circleId = 0
    private var isEdit = false

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circleId = it.getInt(Const.TAG_CIRCLE_ID)
            postType = it.getSerializable(Const.TAG_POST_TYPE) as PostType
            isEdit =
                it.getBoolean(Const.FLAG_EDIT_SCREEN).also { isEdit ->
                    if (isEdit) {
                        post = it.getSerializable(Const.TAG_CIRCLE_POST) as PostModel
                    }
                }
        }
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireActivity(), requireContext())
        initListener(requireContext())
        handleEventFlow()
    }

    private fun initView(
        parentActivity: Activity,
        context: Context,
    ) {
        initToolbar(context)
        hideBtmNav(parentActivity)
        loadPostContentWhenIsEdit(context)
    }

    private fun initToolbar(context: Context) {
        val title =
            when (postType) {
                PostType.NOTICE ->
                    if (isEdit) {
                        context.getString(R.string.title_edit_notice)
                    } else {
                        context.getString(R.string.title_upload_notice)
                    }
                PostType.POST ->
                    if (isEdit) {
                        context.getString(R.string.title_edit_post)
                    } else {
                        context.getString(R.string.title_upload_post)
                    }
            }

        binding.txtTbTitle.text = title
    }

    private fun hideBtmNav(activity: Activity) {
        activity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    private fun loadPostContentWhenIsEdit(context: Context) {
        if (isEdit) {
            binding.edtPostContent.setText(post.content)
            if (post.imgUrl == null) {
                // 이미지 추가 레이아웃 비활성화
                binding.llPostImg.isVisible = false
            } else {
                // 편집 화면에서 이미지 수정 기능 비활성화
                glideProvider.fetchImage(
                    post.imgUrl!!,
                    context,
                    binding.btnAddPostImage,
                )
                binding.txtAddPostImage.isVisible = false
                binding.btnAddPostImage.background = null
            }
        }
    }

    private fun initListener(context: Context) {
        setBtnCancelListener()
        setBtnUploadListener()
        setBtnAddPostImageListener()
        setBtnRemovePostImageListener(context)
    }

    private fun setBtnCancelListener() {
        binding.btnCancel.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun setBtnUploadListener() {
        binding.btnUpload.setOnClickListener {
            if (isEdit) {
                viewModel.edit(post.id, binding.edtPostContent.text.toString())
            } else {
                viewModel.upload(binding.edtPostContent.text.toString())
            }
        }
    }

    private fun setBtnAddPostImageListener() {
        binding.btnAddPostImage.setOnClickListener {
            // 편집 화면에서는 이미지 수정 기능 비활성화
            if (!isEdit) {
                pickMedia.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(PHOTO_MIME_TYPE)),
                )
            }
        }
    }

    private fun setBtnRemovePostImageListener(context: Context) {
        binding.btnRemovePostImage.setOnClickListener {
            viewModel.removePostImage()
            binding.btnAddPostImage.setImageResource(R.drawable.ic_add)
            binding.btnRemovePostImage.isVisible = false
            binding.txtAddPostImage.isVisible = true
            binding.btnAddPostImage.background =
                ContextCompat.getDrawable(context, R.drawable.bg_dotted_rounded_rectangle)
        }
    }

    private fun handleEventFlow() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        super.handleEvent(it)
                    }
                }
                launch {
                    viewModel.screenFlow.collect {
                        handleScreenFlow(it)
                    }
                }
            }
        }
    }

    private fun handleScreenFlow(screenFlow: UploadPostScreen) {
        binding.pgbLoading.isVisible = screenFlow is UploadPostScreen.Loading
        when (screenFlow) {
            is UploadPostScreen.Success -> {
                requestRefreshToPreviousScreen()
                sendUserToPreviousScreen()
            }
            else -> {}
        }
    }

    private fun requestRefreshToPreviousScreen() {
        val flag = if (isEdit) Const.FLAG_CIRCLE_POST_DATA_CHANGED else Const.FLAG_CIRCLE_POST_DATA_ADDED

        findNavController().previousBackStackEntry?.savedStateHandle?.apply {
            set(flag, true)

            // 수정 case 인 경우 원본 post id, content 전달
            if (isEdit) {
                set(Const.TAG_POST, post)
                set(Const.TAG_POST_CONTENT, viewModel.postEditResult.content)
            }
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    companion object {
        private const val PHOTO_MIME_TYPE = "image/jpeg"
    }
}
