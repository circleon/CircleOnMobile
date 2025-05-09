package com.developeek.circleon.view.screen.home

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.PopupMenu
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentCircleDetailPostDetailBinding
import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.PostDetailAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewHideSoftInputListener
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.home.CircleDetailPostDetailViewModel
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostDetailViewModelImpl
import com.developeek.circleon.view.widget.CircleRequestAlertDialog
import com.developeek.circleon.view.widget.EditCommentAlertDialog
import com.developeek.circleon.view.widget.ErrorAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
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
                    it.create(circleId, post)
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

    override fun onCreateAnimator(
        transit: Int,
        enter: Boolean,
        nextAnim: Int,
    ): Animator? {
        if (nextAnim == R.animator.slide_in_right) {
            val animator = AnimatorInflater.loadAnimator(requireContext(), nextAnim)
            animator.addListener(
                onStart = {
                    hideBtmNavWhenVisible(requireActivity())
                },
            )

            return animator
        }
        return super.onCreateAnimator(transit, enter, nextAnim)
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
        postponeEnterTransition()

        initView(requireActivity(), requireContext())
        initObserver(requireActivity(), requireContext())
        initListener(requireContext())
    }

    private fun initView(
        parentActivity: Activity,
        context: Context,
    ) {
        initToolbar()
        initRecyclerView(context)
    }

    private fun initToolbar() {
        if (post.isNotice()) {
            binding.txtTbTitle.text = TITLE_NOTICE
        } else {
            binding.txtTbTitle.text = TITLE_POST
        }
    }

    private fun initRecyclerView(context: Context) {
        binding.rvPostDetail.adapter =
            PostDetailAdapter(
                context,
                glideProvider,
                postOverflowListenerInitializer =
                    object : ItemListenerInitializer<PostModel> {
                        override fun initialize(item: PostModel) {}

                        override fun initialize(
                            item: PostModel,
                            view: View?,
                        ) {
                            initPostOverflowMenuAndShow(context, item, view!!)
                        }
                    },
                commentOverflowListenerInitializer =
                    object : ItemListenerInitializer<CommentModel> {
                        override fun initialize(item: CommentModel) {}

                        override fun initialize(
                            item: CommentModel,
                            view: View?,
                        ) {
                            initCommentOverflowMenuAndShow(context, item, view!!)
                        }
                    },
                userId = userManager.getUser()?.id,
            )
        binding.rvPostDetail.layoutManager = LinearLayoutManager(context)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvPostDetail.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun initPostOverflowMenuAndShow(
        context: Context,
        post: PostModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(context, view) {}
        val userId = userManager.getUser()?.id

        userId?.let {
            if (it == post.author.id) {
                popupMenu.inflate(R.menu.menu_author_post_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.delete_post),
                    ContextCompat.getColor(context, R.color.error),
                )
            } else {
                popupMenu.inflate(R.menu.menu_post_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.report_post),
                    ContextCompat.getColor(context, R.color.error),
                )
            }
        }

        popupMenu.setOnMenuItemClickListener(postOverflowMenuItemClickListener(context, post))
        popupMenu.show()
    }

    private fun postOverflowMenuItemClickListener(
        context: Context,
        item: PostModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.edit_post -> {
                sendUserToEditPostScreen(circleId, item)
            }

            R.id.delete_post -> {
                SingleMessageAlertDialog(
                    context,
                    if (item.isNotice()) MESSAGE_DELETE_NOTICE else MESSAGE_DELETE_POST,
                    ContextCompat.getString(context, R.string.btn_delete),
                    positiveListener = {
                        viewModel.delete()
                    },
                ).show()
            }

            R.id.report_post -> {
                CircleRequestAlertDialog(
                    context,
                    title = ContextCompat.getString(context, R.string.title_report_dialog),
                    positiveButton = ContextCompat.getString(context, R.string.menu_report),
                    positiveListenerInitializer =
                        object : ItemListenerInitializer<String> {
                            override fun initialize(item: String) {
                                viewModel.reportPost(item)
                            }

                            override fun initialize(
                                item: String,
                                view: View?,
                            ) {}
                        },
                ).show()
            }
        }
        true
    }

    private fun sendUserToEditPostScreen(
        circleId: Int,
        item: PostModel,
    ) {
        val bundle = Bundle()

        bundle.putInt(Const.TAG_CIRCLE_ID, circleId)
        bundle.putSerializable(Const.TAG_POST_TYPE, item.type)
        bundle.putSerializable(Const.TAG_CIRCLE_POST, item)
        bundle.putBoolean(Const.FLAG_EDIT_SCREEN, true) // 수정 기능 전용 활성화
        findNavController().navigate(R.id.action_circleDetailPostDetailFragment_to_uploadPostFragment, bundle)
    }

    private fun initCommentOverflowMenuAndShow(
        context: Context,
        comment: CommentModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(context, view) {}
        val userId = userManager.getUser()?.id

        userId?.let {
            if (it == comment.author.id) {
                popupMenu.inflate(R.menu.menu_author_comment_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.delete_comment),
                    ContextCompat.getColor(context, R.color.error),
                )
            } else {
                popupMenu.inflate(R.menu.menu_comment_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.report_comment),
                    ContextCompat.getColor(context, R.color.error),
                )
            }
        }
        popupMenu.setOnMenuItemClickListener(commentOverflowMenuItemClickListener(context, comment))
        popupMenu.show()
    }

    private fun commentOverflowMenuItemClickListener(
        context: Context,
        comment: CommentModel,
    ) = PopupMenu.OnMenuItemClickListener {
        viewModel.saveScrollState(binding.rvPostDetail.layoutManager?.onSaveInstanceState())
        when (it.itemId) {
            R.id.edit_comment -> {
                EditCommentAlertDialog(
                    context,
                    comment.content,
                    object : ItemListenerInitializer<String> {
                        override fun initialize(item: String) {
                            viewModel.editComment(comment.id, item)
                        }

                        override fun initialize(
                            item: String,
                            view: View?,
                        ) {}
                    },
                ).show()
            }

            R.id.delete_comment -> {
                SingleMessageAlertDialog(
                    context,
                    MESSAGE_DELETE_COMMENT,
                    ContextCompat.getString(context, R.string.btn_delete),
                    positiveListener = {
                        viewModel.deleteComment(comment.id)
                    },
                ).show()
            }

            R.id.report_comment -> {
                CircleRequestAlertDialog(
                    context,
                    title = ContextCompat.getString(context, R.string.title_report_dialog),
                    positiveButton = ContextCompat.getString(context, R.string.menu_report),
                    positiveListenerInitializer =
                        object : ItemListenerInitializer<String> {
                            override fun initialize(item: String) {
                                viewModel.reportComment(comment.id, item)
                            }

                            override fun initialize(
                                item: String,
                                view: View?,
                            ) {}
                        },
                ).show()
            }
        }
        true
    }

    private fun initObserver(
        parentActivity: Activity,
        context: Context,
    ) {
        viewModel.state.observe(
            viewLifecycleOwner,
            stateObserver(parentActivity, context),
        )
        viewModel.uploadCommentState.observe(
            viewLifecycleOwner,
            uploadCommentStateObserver(parentActivity, context),
        )
        viewModel.editCommentState.observe(
            viewLifecycleOwner,
            editCommentStateObserver(parentActivity, context),
        )
        viewModel.deleteCommentState.observe(
            viewLifecycleOwner,
            deleteCommentStateObserver(parentActivity, context),
        )
        viewModel.deletePostState.observe(
            viewLifecycleOwner,
            deletePostStateObserver(parentActivity, context),
        )
        viewModel.scrollOver.observe(
            viewLifecycleOwner,
            scrollOverObserver(),
        )
        viewModel.reportState.observe(
            viewLifecycleOwner,
            reportStateObserver(parentActivity, context),
        )
        // 정보 수정 여부 감지
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>(Const.FLAG_CIRCLE_POST_DATA_CHANGED)
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    requestRefreshToPreviousScreen()
                    sendUserToPreviousScreen()
                }
            }
    }

    private fun stateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        binding.pgbLoading.isVisible = it is UiState.Loading
        if (it !is UiState.Loading && it !is UiState.Success) {
            startPostponedEnterTransition()
        }
        when (it) {
            UiState.Success -> {
                toggleView(binding.rvPostDetail)
                loadContents(parentActivity)
            }
            UiState.AuthenticationError -> {
                sendUserToLoginScreen(parentActivity)
                showErrorToast(context)
            }
            UiState.ServiceError -> {
                toggleView(binding.llServiceError)
                showErrorDialog(context)
            }
            else -> {}
        }
    }

    private fun hideBtmNavWhenVisible(parentActivity: Activity) {
        val btmNav = parentActivity.findViewById<BottomNavigationView>(R.id.btmNav)

        if (btmNav.isVisible) btmNav.isVisible = false
    }

    private fun showErrorToast(context: Context) {
        if (ErrorToast.previousFinished()) {
            ErrorToast(context, viewModel.error).show()
        }
    }

    private fun showErrorDialog(context: Context) {
        ErrorAlertDialog(context, viewModel.error).show()
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun loadContents(parentActivity: Activity) {
        binding.rvPostDetail.adapter?.let {
            (it as PostDetailAdapter).update(viewModel.contents) {
                viewModel.currentScrollState?.let {
                    binding.rvPostDetail.layoutManager?.onRestoreInstanceState(viewModel.currentScrollState)
                } ?: binding.rvPostDetail.scrollToPosition(0)
                startPostponedEnterTransition()
            }
        }
    }

    private fun uploadCommentStateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        binding.pgbLoading.isVisible = it is UiState.Loading
        when (it) {
            UiState.Success -> {
                requestRefreshToPreviousScreen()
                loadContents(parentActivity)
                hideSoftInput(context, binding.edtComment)
                binding.edtComment.text?.clear()
            }
            UiState.AuthenticationError -> {
                sendUserToLoginScreen(parentActivity)
                showErrorToast(context)
            }
            UiState.ServiceError -> {
                showErrorDialog(context)
            }
            else -> {}
        }
    }

    private fun editCommentStateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        binding.pgbLoading.isVisible = it is UiState.Loading
        when (it) {
            UiState.Success -> {
                loadContents(parentActivity)
                hideSoftInput(context, binding.edtComment)
            }
            UiState.AuthenticationError -> {
                sendUserToLoginScreen(parentActivity)
                showErrorToast(context)
            }
            UiState.ServiceError -> {
                showErrorDialog(context)
            }
            else -> {}
        }
    }

    private fun deleteCommentStateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        binding.pgbLoading.isVisible = it is UiState.Loading
        when (it) {
            UiState.Success -> {
                requestRefreshToPreviousScreen()
                loadContents(parentActivity)
            }
            UiState.AuthenticationError -> {
                sendUserToLoginScreen(parentActivity)
                showErrorToast(context)
            }
            UiState.ServiceError -> {
                ErrorAlertDialog(context, viewModel.error).show()
            }
            else -> {}
        }
    }

    private fun hideSoftInput(
        context: Context,
        view: EditText,
    ) {
        val imm = context.getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun deletePostStateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        binding.pgbLoading.isVisible = it is UiState.Loading
        when (it) {
            UiState.Success -> {
                requestRefreshToPreviousScreen()
                sendUserToPreviousScreen()
            }
            UiState.AuthenticationError -> {
                sendUserToLoginScreen(parentActivity)
                showErrorToast(context)
            }
            UiState.ServiceError -> {
                ErrorAlertDialog(context, viewModel.error).show()
            }
            else -> {}
        }
    }

    private fun scrollOverObserver() =
        Observer<Boolean> { completed ->
            if (completed) {
                binding.rvPostDetail.removeOnScrollListener(viewModel.scrollListener)
                binding.rvPostDetail.addOnScrollListener(viewModel.scrollListener)
                binding.rvPostDetail.adapter?.let {
                    (it as PostDetailAdapter).update(viewModel.contents) {}
                }
            }
        }

    private fun reportStateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        binding.pgbLoading.isVisible = it is UiState.Loading
        when (it) {
            UiState.AuthenticationError -> {
                sendUserToLoginScreen(parentActivity)
                showErrorToast(context)
            }
            UiState.ServiceError -> {
                showErrorDialog(context)
            }
            else -> {}
        }
    }

    private fun requestRefreshToPreviousScreen() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_CIRCLE_POST_DATA_CHANGED, true)
    }

    private fun initListener(context: Context) {
        setRvCircleCommentListener(context)
        setBtnBackListener()
        setBtnRegisterCommentListener()
        setBtnRetryListener()
    }

    private fun setRvCircleCommentListener(context: Context) {
        binding.rvPostDetail.addOnScrollListener(viewModel.scrollListener)
        binding.rvPostDetail.addOnScrollListener(RecyclerViewHideSoftInputListener(context))
        (viewModel.scrollListener as RecyclerViewInfiniteScrollListener).setScrollEndListener {
            if (!viewModel.comments.isLastPage()) {
                addScrollLoadingItemAndLoad()
                // scrollOver 시 문제가 발생하더라도 정상으로 돌아온 경우 스크롤 복원하기 위함
                viewModel.saveScrollState(binding.rvPostDetail.layoutManager?.onSaveInstanceState())
            }
        }
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvPostDetail.adapter?.let {
            (it as PostDetailAdapter).update(viewModel.contents + CommentModel.emptyInstance()) {}
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

    private fun setBtnRegisterCommentListener() {
        binding.btnUploadComment.setOnClickListener {
            viewModel.saveScrollState(binding.rvPostDetail.layoutManager?.onSaveInstanceState())
            viewModel.uploadComment(binding.edtComment.text.toString())
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    private fun toggleView(view: View) {
        binding.rvPostDetail.isVisible = view == binding.rvPostDetail
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    companion object {
        private const val TITLE_NOTICE = "공지사항 상세보기"
        private const val TITLE_POST = "게시글 상세보기"
        private const val MESSAGE_DELETE_NOTICE = "공지사항을 삭제할까요?"
        private const val MESSAGE_DELETE_POST = "게시글을 삭제할까요?"
        private const val MESSAGE_DELETE_COMMENT = "댓글을 삭제할까요?"
    }
}
