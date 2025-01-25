package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentUploadPostBinding
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils.toJPEG
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.UploadPostViewModel
import com.developeek.circleon.view.viewmodelimpl.UploadPostViewModelImpl
import com.developeek.circleon.view.widget.ErrorAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import javax.inject.Inject

@AndroidEntryPoint
class UploadPostFragment : Fragment() {
    private lateinit var binding: FragmentUploadPostBinding
    private var circleId = 0
    private lateinit var postType: PostType
    private var editOrNot = false
    private lateinit var post: PostModel
    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                uri ->
            uri?.let {
                glideProvider.loadImage(it, requireActivity(), binding.btnAddPostImage)
                viewModel.setPostImage(it.toJPEG(requireActivity()))
                binding.btnRemovePostImage.isVisible = true
                binding.txtAddPostImage.isVisible = false
                binding.btnAddPostImage.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.bg_small_rounded_rectangle,
                    ),
                )
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

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circleId = it.getInt(Const.TAG_CIRCLE_ID)
            postType = it.getSerializable(Const.TAG_POST_TYPE) as PostType
            editOrNot =
                it.getBoolean(Const.FLAG_EDIT_OR_NOT).also { isEdit ->
                    if (isEdit) {
                        post = it.getSerializable(Const.TAG_CIRCLE_POST) as PostModel
                    }
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUploadPostBinding.inflate(layoutInflater)

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
        initToolbar()
        loadContentWhenEdit(activity)
        hideBtmNav(activity)
    }

    private fun initToolbar() {
        var title = Const.EMPTY_TEXT

        if (postType.isNotice() && editOrNot) {
            title = TITLE_NOTICE_EDIT
        }
        if (postType.isNotice() && !editOrNot) {
            title = TITLE_NOTICE
        }
        if (postType.isPost() && editOrNot) {
            title = TITLE_POST_EDIT
        }
        if (postType.isPost() && !editOrNot) {
            title = TITLE_POST
        }

        binding.txtTbTitle.text = title
    }

    private fun loadContentWhenEdit(activity: Activity) {
        if (editOrNot) {
            binding.edtPostContent.setText(post.content)
            if (post.imgUrl == null) {
                // 이미지 추가 레이아웃 비활성화
                binding.llPostImg.isVisible = false
            } else {
                // 편집 화면에서 이미지 수정 기능 비활성화
                glideProvider.fetchImage(
                    post.imgUrl!!,
                    activity,
                    binding.btnAddPostImage,
                )
                binding.txtAddPostImage.isVisible = false
                binding.btnAddPostImage.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.bg_small_rounded_rectangle,
                    ),
                )
            }
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
            when (it) {
                UiState.Loading -> {
                    binding.pgbContentLoading.isVisible = true
                }
                UiState.Success -> {
                    binding.pgbContentLoading.isVisible = false
                    requestRefreshToPreviousScreen()
                    sendUserToPreviousScreen()
                }
                UiState.AuthenticationError -> {
                    binding.pgbContentLoading.isVisible = false
                    sendUserToLoginScreen(activity)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
                UiState.ServiceError -> {
                    binding.pgbContentLoading.isVisible = false
                    ErrorAlertDialog(activity, viewModel.error).show()
                }
            }
        }

    private fun requestRefreshToPreviousScreen() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_DATA_CHANGED, true)
    }

    private fun sendUserToPreviousScreen() {
        findNavController().navigateUp()
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun initListener(activity: Activity) {
        setBtnCancelListener()
        setBtnUploadListener()
        setBtnAddPostImageListener()
        setBtnRemovePostImageListener(activity)
    }

    private fun setBtnCancelListener() {
        binding.btnCancel.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun setBtnUploadListener() {
        binding.btnUpload.setOnClickListener {
            if (editOrNot) {
                viewModel.edit(post.id, binding.edtPostContent.text.toString())
            } else {
                viewModel.upload(binding.edtPostContent.text.toString())
            }
        }
    }

    private fun setBtnAddPostImageListener() {
        binding.btnAddPostImage.setOnClickListener {
            // 편집 화면에서는 이미지 수정 기능 비활성화
            if (!editOrNot) {
                val mimeType = "image/jpeg"
                pickMedia.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType)),
                )
            }
        }
    }

    private fun setBtnRemovePostImageListener(activity: Activity) {
        binding.btnRemovePostImage.setOnClickListener {
            viewModel.removePostImage()
            binding.btnAddPostImage.setImageResource(R.drawable.ic_add)
            binding.btnRemovePostImage.isVisible = false
            binding.txtAddPostImage.isVisible = true
            binding.btnAddPostImage.setBackgroundDrawable(
                ContextCompat.getDrawable(activity, R.drawable.bg_dotted_rounded_rectangle),
            )
        }
    }

    companion object {
        private const val TITLE_NOTICE = "공지사항 작성"
        private const val TITLE_NOTICE_EDIT = "공지사항 수정"
        private const val TITLE_POST = "게시글 작성"
        private const val TITLE_POST_EDIT = "게시글 수정"
    }
}
