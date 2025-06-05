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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentCircleDetailPostDetailBinding
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.model.BaseModel
import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.model.UserModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.adapter.PostDetailAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.auth.LoginActivity
import com.developeek.circleon.view.viewmodel.home.CircleDetailPostDetailViewModel
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostDetailScreen
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostDetailViewModelImpl
import com.developeek.circleon.view.widget.CircleRequestAlertDialog
import com.developeek.circleon.view.widget.EditCommentAlertDialog
import com.developeek.circleon.view.widget.PositiveAlertDialog
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch
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

    private fun hideBtmNavWhenVisible(parentActivity: Activity) {
        val btmNav = parentActivity.findViewById<BottomNavigationView>(R.id.btmNav)

        if (btmNav.isVisible) btmNav.isVisible = false
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

        initView(requireActivity())
        initRefreshObserver()
        initListener()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        handleEvent(it, requireActivity(), requireContext())
                    }
                }
                launch {
                    viewModel.screenFlow.collect {
                        handleScreenFlow(it, requireContext())
                    }
                }
            }
        }
    }

    private fun initView(context: Context) {
        initToolbar(context)
        initRecyclerView(context)
    }

    private fun initToolbar(context: Context) {
        val title =
            when (post.type) {
                PostType.POST -> context.getString(R.string.title_post_detail)
                PostType.NOTICE -> context.getString(R.string.title_notice_detail)
            }

        binding.txtTbTitle.text = title
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

        userManager.getUser()?.let {
            inflatePostPopupByUser(popupMenu, post, it, context)
        }
        popupMenu.setOnMenuItemClickListener(postOverflowMenuItemClickListener(context, post))
        popupMenu.show()
    }

    private fun inflatePostPopupByUser(
        popupMenu: PopupMenu,
        post: PostModel,
        user: UserModel,
        context: Context,
    ) {
        if (user.id == post.author.authorId) {
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

    private fun postOverflowMenuItemClickListener(
        context: Context,
        item: PostModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.edit_post -> {
                sendUserToEditPostScreen(circleId, item)
            }

            R.id.delete_post -> {
                showDeletePostDialog(item, context)
            }

            R.id.report_post -> {
                showReportPostDialog(context)
            }
        }
        true
    }

    private fun showDeletePostDialog(
        post: PostModel,
        context: Context,
    ) {
        PositiveAlertDialog(
            context,
            when (post.type) {
                PostType.POST -> context.getString(R.string.message_delete_post)
                PostType.NOTICE -> context.getString(R.string.message_delete_notice)
            },
            context.getString(R.string.btn_delete),
            positiveListener = {
                viewModel.delete()
            },
        ).show()
    }

    private fun showReportPostDialog(context: Context) {
        CircleRequestAlertDialog(
            context,
            title = context.getString(R.string.title_report_dialog),
            positiveButton = context.getString(R.string.menu_report),
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

        userManager.getUser()?.let {
            inflateCommentPopupByUser(popupMenu, comment, it, context)
        }
        popupMenu.setOnMenuItemClickListener(commentOverflowMenuItemClickListener(context, comment))
        popupMenu.show()
    }

    private fun inflateCommentPopupByUser(
        popupMenu: PopupMenu,
        comment: CommentModel,
        user: UserModel,
        context: Context,
    ) {
        if (user.id == comment.author.authorId) {
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

    private fun commentOverflowMenuItemClickListener(
        context: Context,
        comment: CommentModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.edit_comment -> {
                showEditCommentDialog(comment, context)
            }

            R.id.delete_comment -> {
                showDeleteCommentDialog(comment, context)
            }

            R.id.report_comment -> {
                showReportCommentDialog(comment, context)
            }
        }
        true
    }

    private fun showEditCommentDialog(
        comment: CommentModel,
        context: Context,
    ) {
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

    private fun showDeleteCommentDialog(
        comment: CommentModel,
        context: Context,
    ) {
        PositiveAlertDialog(
            context,
            context.getString(R.string.message_delete_comment),
            context.getString(R.string.btn_delete),
            positiveListener = {
                viewModel.deleteComment(comment.id)
            },
        ).show()
    }

    private fun showReportCommentDialog(
        comment: CommentModel,
        context: Context,
    ) {
        CircleRequestAlertDialog(
            context,
            title = context.getString(R.string.title_report_dialog),
            positiveButton = context.getString(R.string.menu_report),
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

    private fun initRefreshObserver() {
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

    private fun initListener() {
        setRvCircleCommentListener()
        setBtnBackListener()
        setBtnRegisterCommentListener()
        setBtnRetryListener()
    }

    private fun setRvCircleCommentListener() {
        val scrollListener =
            RecyclerViewInfiniteScrollListener().apply {
                setScrollEndListener {
                    if (!viewModel.isLastPage) {
                        addScrollLoadingItemAndLoad()
                    }
                }
            }

        binding.rvPostDetail.addOnScrollListener(scrollListener)
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvPostDetail.adapter?.let {
            (it as PostDetailAdapter).addLoadingItem()
            viewModel.scrollOver()
        }
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().popBackStack()
    }

    private fun setBtnRegisterCommentListener() {
        binding.btnUploadComment.setOnClickListener {
            viewModel.uploadComment(binding.edtComment.text.toString())
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.showLoadingAndRefresh()
        }
    }

    private fun handleEvent(
        event: Event,
        parentActivity: Activity,
        context: Context,
    ) {
        binding.pgbLoading.isVisible = event is Event.ShowProcessing
        when (event) {
            is Event.SendToLoginScreen -> sendUserToLoginScreen(parentActivity)
            is Event.ShowToast -> showToast(event, context)
            is Event.ShowDialog -> showDialog(event, context)
            else -> {}
        }
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showToast(
        event: Event.ShowToast,
        context: Context,
    ) {
        if (SingleMessageToast.previousFinished()) {
            SingleMessageToast(context, event.message).show()
        }
    }

    private fun showDialog(
        event: Event.ShowDialog,
        context: Context,
    ) {
        SingleMessageAlertDialog(context, event.message).show()
    }

    private fun handleScreenFlow(
        screenFlow: CircleDetailPostDetailScreen,
        context: Context,
    ) {
        when (screenFlow) {
            is CircleDetailPostDetailScreen.SuccessView -> showSuccessView(screenFlow, context)
            is CircleDetailPostDetailScreen.LoadingView -> showLoadingView()
            is CircleDetailPostDetailScreen.ErrorView -> showErrorView()
        }
    }

    private fun showSuccessView(
        screenFlow: CircleDetailPostDetailScreen.SuccessView,
        context: Context,
    ) {
        if (screenFlow.hasDeleted) {
            requestRefreshToPreviousScreen()
            sendUserToPreviousScreen()
            return
        }

        switchView(binding.rvPostDetail)
        hideSoftInput(context, binding.edtComment)
        binding.edtComment.setText(Const.EMPTY_TEXT)
        loadContentsAndDoAfter(
            screenFlow.contents,
            after = {
                startPostponedEnterTransition()
            },
        )
    }

    private fun hideSoftInput(
        context: Context,
        view: EditText,
    ) {
        val imm = context.getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showLoadingView() {
        switchView(binding.contentLoading)
    }

    private fun showErrorView() {
        switchView(binding.llServiceError)
        startPostponedEnterTransition()
    }

    private fun loadContentsAndDoAfter(
        contents: Models<BaseModel>,
        after: () -> Unit,
    ) {
        binding.rvPostDetail.adapter?.let {
            (it as PostDetailAdapter).update(contents) {
                after()
            }
        }
    }

    private fun requestRefreshToPreviousScreen() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_CIRCLE_POST_DATA_CHANGED, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    private fun switchView(view: View) {
        binding.rvPostDetail.isVisible = view == binding.rvPostDetail
        binding.contentLoading.isVisible = view == binding.contentLoading
        binding.llServiceError.isVisible = view == binding.llServiceError
    }
}
