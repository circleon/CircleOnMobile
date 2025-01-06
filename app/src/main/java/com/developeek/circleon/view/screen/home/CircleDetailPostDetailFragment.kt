package com.developeek.circleon.view.screen.home

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentCircleDetailPostDetailBinding
import com.developeek.circleon.databinding.ItemPostCommentBinding
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.CircleDetailPostDetailViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailPostDetailViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailPostDetailFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailPostDetailBinding
    private var circleId = 0
    private lateinit var post: PostModel
    private val viewModel: CircleDetailPostDetailViewModel by viewModels<CircleDetailPostDetailViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<CircleDetailPostDetailViewModelImpl.CircleDetailPostDetailViewModelFactory> {
                    it.create(circleId, post.id)
                }
        },
    )

    @Inject
    lateinit var glideProvider: GlideProvider

    @Inject
    lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circleId = it.getInt(Const.TAG_CIRCLE_ID)
            post = it.getSerializable(Const.TAG_CIRCLE_POST) as PostModel
        }

        // post 상세 화면에서만 adjust resize mode 로 전환
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailPostDetailBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireActivity())
        initObserver(requireActivity())
        initListener()
    }

    private fun initView(activity: Activity) {
        loadAuthor(activity)
        loadPost(activity)
    }

    private fun loadAuthor(activity: Activity) {
        binding.txtAuthorName.text = post.author.name
        binding.txtCreated.text =
            post.createdAt.format(
                DateTimeFormatter.ofPattern(CREATED_DATE_FORMAT),
            )
        post.author.profileUrl?.let {
            glideProvider.callImage(it, activity, binding.imgAuthorProfile)
        } ?: binding.imgAuthorProfile.setImageResource(R.drawable.ic_author_placeholder)
    }

    private fun loadPost(activity: Activity) {
        binding.txtPostContent.text = post.content
        post.imgUrl?.let {
            binding.imgPost.isVisible = true
            glideProvider.callImage(it, activity, binding.imgPost)
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
                UiState.Loading -> {
                    toggleView(binding.pgbLoading)
                }
                UiState.Success -> {
                    toggleView(binding.svPostDetail)
                    if (viewModel.comments.isEmpty()) {
                        toggleComment(binding.txtNoComment)
                    } else {
                        toggleComment(binding.llComment)
                        loadComments(activity)
                    }
                }
                UiState.AuthenticationError -> {
                    sendUserToLoginScreen(activity)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
                UiState.ServiceError -> {
                    toggleView(binding.llServiceError)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
            }
        }

    private fun loadComments(activity: Activity) {
        viewModel.comments.get().forEach {
            val itemBinding = ItemPostCommentBinding.inflate(layoutInflater)

            itemBinding.apply {
                txtAuthorName.text = it.author.name
                txtCreated.text = it.createdAt.format(DateTimeFormatter.ofPattern(CREATED_DATE_FORMAT))
                txtComment.text = it.content
                it.author.profileUrl?.let {
                    glideProvider.callImage(it, activity, imgAuthorProfile)
                } ?: imgAuthorProfile.setImageResource(R.drawable.ic_author_placeholder)
            }

            binding.llComment.addView(itemBinding.root)
        }
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun initListener() {
        setBtnBackListener()
        setBtnRetryListener()
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().navigateUp()
    }

    override fun onCreateAnimator(
        transit: Int,
        enter: Boolean,
        nextAnim: Int,
    ): Animator? {
        if (nextAnim == R.animator.slide_end_to_start) {
            val animState = arguments?.getBoolean(Const.TAG_ANIM_STATE)
            animState?.let {
                val animator = AnimatorInflater.loadAnimator(context, nextAnim) as AnimatorSet
                if (it) {
                    animator.addListener(onEnd = {
                        viewModel.updateUiState()
                    })

                    return animator
                } else {
                    changeAnimDuration(animator, 0)

                    return animator
                }
            }
        }
        return super.onCreateAnimator(transit, enter, nextAnim)
    }

    private fun changeAnimDuration(
        animator: AnimatorSet,
        duration: Long,
    ) {
        animator.childAnimations.forEach {
            it.setDuration(duration)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        arguments?.putBoolean(Const.TAG_ANIM_STATE, false)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    private fun toggleView(view: View) {
        binding.svPostDetail.visibility = visibleWhenTrue(view == binding.svPostDetail)
        binding.pgbLoading.visibility = visibleWhenTrue(view == binding.pgbLoading)
        binding.llServiceError.visibility = visibleWhenTrue(view == binding.llServiceError)
    }

    private fun toggleComment(view: View) {
        binding.llComment.visibility = visibleWhenTrue(view == binding.llComment)
        binding.txtNoComment.visibility = visibleWhenTrue(view == binding.txtNoComment)
    }

    private fun visibleWhenTrue(state: Boolean) = if (state) View.VISIBLE else View.GONE

    companion object {
        private const val CREATED_DATE_FORMAT = "M월 d일 hh:mm"
    }
}
