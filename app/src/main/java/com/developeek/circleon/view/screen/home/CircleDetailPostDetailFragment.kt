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
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentCircleDetailPostDetailBinding
import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.PostCommentAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.CircleDetailPostDetailViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailPostDetailViewModelImpl
import com.developeek.circleon.view.widget.DeleteAlertDialog
import com.developeek.circleon.view.widget.ErrorAlertDialog
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
        initRecyclerView(activity)
        hidePostOverFlowOrNot()
        loadAuthor(activity)
        loadPost(activity)
    }

    private fun initToolbar() {
        if (post.isNotice()) {
            binding.txtTbTitle.text = TITLE_NOTICE
        } else {
            binding.txtTbTitle.text = TITLE_POST
        }
    }

    private fun initRecyclerView(activity: Activity) {
        binding.rvComment.adapter =
            PostCommentAdapter(
                activity,
                glideProvider,
                overflowListenerInitializer =
                    object : ItemListenerInitializer<CommentModel> {
                        override fun initialize(item: CommentModel) {}

                        override fun initialize(
                            item: CommentModel,
                            view: View?,
                        ) {
                            initCommentOverflowMenuAndShow(activity, item, view!!)
                        }
                    },
                userId = userManager.getUser()?.id,
            )
        binding.rvComment.layoutManager = LinearLayoutManager(activity)
    }

    private fun initCommentOverflowMenuAndShow(
        activity: Activity,
        comment: CommentModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(activity, view) {}
        popupMenu.inflate(R.menu.menu_comment_settings)
        popupMenu.setOnMenuItemClickListener(commentOverflowMenuItemClickListener(activity, comment))
        Utils.changeMenuItemTextColor(
            popupMenu.menu.findItem(R.id.delete_comment),
            ContextCompat.getColor(activity, R.color.error),
        )

        popupMenu.show()
    }

    private fun commentOverflowMenuItemClickListener(
        activity: Activity,
        comment: CommentModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.modify_comment -> {
            }
            R.id.delete_comment -> {
                DeleteAlertDialog(activity, MESSAGE_DELETE_COMMENT) {
                    viewModel.deleteComment(comment.id)
                }.show()
            }
        }
        true
    }

    private fun hidePostOverFlowOrNot() {
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
        viewModel.scrollOver.observe(
            viewLifecycleOwner,
            scrollOverObserver(),
        )
        viewModel.deletePostState.observe(
            viewLifecycleOwner,
            deletePostStateObserver(activity),
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
                        toggleView(binding.rvComment)
                        loadComments()
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

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun loadComments() {
        binding.rvComment.adapter?.let {
            (it as PostCommentAdapter).update(viewModel.comments) {
                viewModel.currentScrollState?.let {
                    binding.rvComment.layoutManager?.onRestoreInstanceState(viewModel.currentScrollState)
                } ?: binding.rvComment.scrollToPosition(0)
            }
        }
    }

    private fun hideSoftInput(
        activity: Activity,
        view: EditText,
    ) {
        val imm = activity.getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun scrollOverObserver() =
        Observer<Boolean> { completed ->
            if (completed) {
                binding.rvComment.adapter?.let {
                    (it as PostCommentAdapter).update(viewModel.comments) {}
                }
            }
        }

    private fun deletePostStateObserver(activity: Activity) =
        Observer<Boolean> {
            if (it) {
                requestRefreshToPreviousScreen()
                sendUserToPreviousScreen()
            } else {
                ErrorAlertDialog(activity, viewModel.error).show()
            }
        }

    private fun requestRefreshToPreviousScreen() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_DATA_CHANGED, true)
    }

    private fun initListener(activity: Activity) {
        setRvCircleCommentListener()
        setBtnBackListener()
        setBtnPostOverFlowMenuListener(activity)
        setBtnRegisterCommentListener()
        setBtnRetryListener()
    }

    private fun setRvCircleCommentListener() {
        binding.svPostDetail.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (!v.canScrollVertically(1) && !viewModel.comments.isLastPage()) {
                addScrollLoadingItemAndLoad()
                // scrollOver 시 문제가 발생하더라도 정상으로 돌아온 경우 스크롤 복원하기 위함
                viewModel.saveScrollState(binding.rvComment.layoutManager?.onSaveInstanceState())
            }
        }
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvComment.adapter?.let {
            (it as PostCommentAdapter).update(viewModel.comments.add(CommentModel.emptyInstance())) {}
        }
        viewModel.scrollOver()
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
        post: PostModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(activity, view) {}
        popupMenu.inflate(R.menu.menu_post_settings)
        popupMenu.setOnMenuItemClickListener(postOverflowMenuItemClickListener(activity, post))
        Utils.changeMenuItemTextColor(
            popupMenu.menu.findItem(R.id.delete_post),
            ContextCompat.getColor(activity, R.color.error),
        )

        popupMenu.show()
    }

    private fun postOverflowMenuItemClickListener(
        activity: Activity,
        post: PostModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.modify_post -> {
                Toast.makeText(activity, "수정하기", Toast.LENGTH_SHORT).show()
            }
            R.id.delete_post -> {
                DeleteAlertDialog(activity, if (post.isNotice()) MESSAGE_DELETE_NOTICE else MESSAGE_DELETE_POST) {
                    viewModel.delete()
                }.show()
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
            val animator = AnimatorInflater.loadAnimator(context, nextAnim) as AnimatorSet
            animator.addListener(onEnd = {
                viewModel.notifyEnterAnimFinishedAndUpdateUI()
            })
            return animator
        }
        return super.onCreateAnimator(transit, enter, nextAnim)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    private fun toggleView(view: View) {
        binding.rvComment.isVisible = view == binding.rvComment
        binding.txtNoComment.isVisible = view == binding.txtNoComment
        binding.pgbLoading.isVisible = view == binding.pgbLoading
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    companion object {
        private const val CREATED_DATE_FORMAT = "M월 d일 a hh:mm"
        private const val TITLE_NOTICE = "공지사항 상세보기"
        private const val TITLE_POST = "게시글 상세보기"
        private const val MESSAGE_DELETE_NOTICE = "공지사항을 삭제할까요?"
        private const val MESSAGE_DELETE_POST = "게시글을 삭제할까요?"
        private const val MESSAGE_DELETE_COMMENT = "댓글을 삭제할까요?"
    }
}
