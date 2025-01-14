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
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentCircleDetailPostDetailBinding
import com.developeek.circleon.databinding.ItemPostCommentBinding
import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.CircleDetailPostDetailViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailPostDetailViewModelImpl
import com.developeek.circleon.view.widget.CustomAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import java.time.format.DateTimeFormatter
import java.util.Locale
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
        initListener(requireActivity())
    }

    private fun initView(activity: Activity) {
        initToolbar()
        hideOverFlowOrNot()
        loadAuthor(activity)
        loadPost(activity)
    }

    private fun initToolbar() {
        if (post.type.isNotice()) {
            binding.txtTbTitle.text = TITLE_NOTICE
        } else {
            binding.txtTbTitle.text = TITLE_POST
        }
    }

    private fun hideOverFlowOrNot() {
        userManager.getUser()?.let {
            binding.btnPostOverflow.isVisible = it.id == post.author.id
        }
    }

    private fun loadAuthor(activity: Activity) {
        binding.txtAuthorName.text = post.author.name
        binding.txtCreated.text =
            post.createdAt.format(
                DateTimeFormatter
                    .ofPattern(CREATED_DATE_FORMAT)
                    .withLocale(Locale.KOREAN),
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
        viewModel.registerCommentState.observe(
            viewLifecycleOwner,
            registerCommentStateObserver(activity),
        )
    }

    private fun stateObserver(activity: Activity) =
        Observer<UiState> {
            when (it) {
                UiState.Loading -> {
                    toggleView(binding.pgbLoading)
                }
                UiState.Success -> {
                    if (viewModel.comments.isEmpty()) {
                        toggleView(binding.txtNoComment)
                    } else {
                        toggleView(binding.llComment)
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
            loadComment(activity, it)
        }
    }

    private fun loadComment(
        activity: Activity,
        comment: CommentModel,
    ) {
        val itemBinding = ItemPostCommentBinding.inflate(layoutInflater)

        itemBinding.apply {
            txtAuthorName.text = comment.author.name
            txtCreated.text =
                comment.createdAt.format(
                    DateTimeFormatter
                        .ofPattern(CREATED_DATE_FORMAT)
                        .withLocale(Locale.KOREAN),
                )
            txtComment.text = comment.content
            comment.author.profileUrl?.let {
                glideProvider.callImage(it, activity, imgAuthorProfile)
            } ?: imgAuthorProfile.setImageResource(R.drawable.ic_author_placeholder)
        }

        binding.llComment.addView(itemBinding.root)
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun registerCommentStateObserver(activity: Activity) =
        Observer<Boolean> {
            if (it) {
                toggleView(binding.llComment)
                loadComment(activity, viewModel.comments.last())
                hideSoftInput(activity, binding.edtComment)
                binding.edtComment.text.clear()
                // 동아리 상세 화면에 댓글 정보 수정 여부 전달
                findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_DATA_CHANGED, true)
            } else {
                CustomAlertDialog(activity, viewModel.error).show()
            }
        }

    private fun hideSoftInput(
        activity: Activity,
        view: EditText,
    ) {
        val imm = activity.getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun initListener(activity: Activity) {
        setBtnBackListener()
        setBtnPostOverFlowMenuListener(activity)
        setBtnRegisterCommentListener()
        setBtnRetryListener()
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().navigateUp()
    }

    private fun setBtnPostOverFlowMenuListener(activity: Activity) {
        binding.btnPostOverflow.setOnClickListener {
            initPostOverflowMenuAndShow(activity, post, binding.btnPostOverflow)
        }
    }

    private fun initPostOverflowMenuAndShow(
        activity: Activity,
        item: PostModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(activity, view) {}
        popupMenu.inflate(R.menu.menu_post_settings)
        popupMenu.setOnMenuItemClickListener(postOverflowMenuItemClickListener(item))
        Utils.changeMenuItemTextColor(
            popupMenu.menu.findItem(R.id.delete_post),
            ContextCompat.getColor(activity, R.color.error),
        )

        popupMenu.show()
    }

    private fun postOverflowMenuItemClickListener(item: PostModel) =
        PopupMenu.OnMenuItemClickListener {
            when (it.itemId) {
                R.id.modify_post -> {
                    Toast.makeText(activity, "수정하기", Toast.LENGTH_SHORT).show()
                }
                R.id.delete_post -> {
                    Toast.makeText(activity, "삭제하기", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

    private fun setBtnRegisterCommentListener() {
        binding.btnRegisterComment.setOnClickListener {
            viewModel.registerComment(binding.edtComment.text.toString())
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    override fun onCreateAnimator(
        transit: Int,
        enter: Boolean,
        nextAnim: Int,
    ): Animator? {
        if (nextAnim == R.animator.slide_end_to_start) {
            val animState = arguments?.getBoolean(Const.FLAG_ANIM_STATE)
            animState?.let {
                val animator = AnimatorInflater.loadAnimator(context, nextAnim) as AnimatorSet
                if (it) {
                    animator.addListener(onEnd = {
                        viewModel.notifyEnterAnimFinishedAndUpdateUI()
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

        arguments?.putBoolean(Const.FLAG_ANIM_STATE, false)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    private fun toggleView(view: View) {
        binding.llComment.isVisible = view == binding.llComment
        binding.txtNoComment.isVisible = view == binding.txtNoComment
        binding.pgbLoading.isVisible = view == binding.pgbLoading
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    companion object {
        private const val CREATED_DATE_FORMAT = "M월 d일 a hh:mm"
        private const val TITLE_NOTICE = "공지사항 상세보기"
        private const val TITLE_POST = "게시글 상세보기"
    }
}
