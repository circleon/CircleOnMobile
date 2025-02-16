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
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import javax.inject.Inject

@AndroidEntryPoint
class UploadPostFragment : Fragment() {
    private lateinit var binding: FragmentUploadPostBinding
    private var circleId = 0
    private lateinit var postType: PostType // 편집이 아닌 상황에서는 item 전달이 되지 않기 때문에 post type 을 별도로 수신
    private var isEdit = false
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
                        R.drawable.bg_rounded_rectangle,
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
            isEdit =
                it.getBoolean(Const.FLAG_IS_EDIT).also { isEdit ->
                    if (isEdit) {
                        post = it.getSerializable(Const.TAG_CIRCLE_POST) as PostModel
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
        hideBtmNav(activity)
        loadPostContentWhenIsEdit(activity)
    }

    private fun initToolbar() {
        var title = Const.EMPTY_TEXT

        if (postType.isNotice() && isEdit) {
            title = TITLE_NOTICE_EDIT
        }
        if (postType.isNotice() && !isEdit) {
            title = TITLE_NOTICE
        }
        if (postType.isPost() && isEdit) {
            title = TITLE_POST_EDIT
        }
        if (postType.isPost() && !isEdit) {
            title = TITLE_POST
        }

        binding.txtTbTitle.text = title
    }

    private fun loadPostContentWhenIsEdit(activity: Activity) {
        if (isEdit) {
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
                        R.drawable.bg_rounded_rectangle,
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
            val loadingIndicator = activity.findViewById<CircularProgressIndicator>(R.id.pgbLoading)
            loadingIndicator.isVisible = it is UiState.Loading
            when (it) {
                UiState.Success -> {
                    requestRefreshToPreviousScreen()
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

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    companion object {
        private const val TITLE_NOTICE = "공지사항 작성"
        private const val TITLE_NOTICE_EDIT = "공지사항 수정"
        private const val TITLE_POST = "게시글 작성"
        private const val TITLE_POST_EDIT = "게시글 수정"
    }
}
