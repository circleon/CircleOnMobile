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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.databinding.FragmentNewPostBinding
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils.toJPEG
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.NewPostViewModel
import com.developeek.circleon.view.viewmodelimpl.NewPostViewModelImpl
import com.developeek.circleon.view.widget.ErrorAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import javax.inject.Inject

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private lateinit var binding: FragmentNewPostBinding
    private var circleId = 0
    private lateinit var postType: PostType
    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                uri ->
            uri?.let {
                glideProvider.loadImage(it, requireActivity(), binding.btnAddPostImage)
                viewModel.setPostImage(it.toJPEG(requireActivity()))
            }
        }
    private val viewModel: NewPostViewModel by viewModels<NewPostViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<NewPostViewModelImpl.NewPostViewModelFactory> {
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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNewPostBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver(requireActivity())
        initListener()
    }

    private fun initView() {
        initToolbar()
    }

    private fun initToolbar() {
        if (postType.isNotice()) {
            binding.txtTbTitle.text = TITLE_NOTICE
        } else {
            binding.txtTbTitle.text = TITLE_POST
        }
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
                UiState.Loading -> {}
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

    private fun initListener() {
        setBtnCancelListener()
        setBtnUploadListener()
        setBtnAddPostImageListener()
    }

    private fun setBtnCancelListener() {
        binding.btnCancel.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun setBtnUploadListener() {
        binding.btnUpload.setOnClickListener {
            viewModel.upload(binding.edtPostContent.text.toString())
        }
    }

    private fun setBtnAddPostImageListener() {
        binding.btnAddPostImage.setOnClickListener {
            val mimeType = "image/jpeg"
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType)))
        }
    }

    companion object {
        private const val TITLE_NOTICE = "공지사항 작성"
        private const val TITLE_POST = "게시글 작성"
    }
}
