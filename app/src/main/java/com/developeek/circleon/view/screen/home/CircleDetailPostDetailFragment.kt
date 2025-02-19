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
import com.developeek.circleon.view.adapter.PostDetailAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewHideSoftInputListener
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.CircleDetailPostDetailViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailPostDetailViewModelImpl
import com.developeek.circleon.view.widget.ContentDeleteAlertDialog
import com.developeek.circleon.view.widget.ErrorAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
import com.developeek.circleon.view.widget.TextInputAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailPostDetailBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onCreateAnimator(
        transit: Int,
        enter: Boolean,
        nextAnim: Int,
    ): Animator? {
        if (nextAnim == R.animator.slide_in_left) {
            val animator = AnimatorInflater.loadAnimator(context, nextAnim) as AnimatorSet
            animator.addListener(
                onEnd = {
                    viewModel.notifyEnterAnimFinishedAndUpdateUI()
                },
            )

            return animator
        }
        return super.onCreateAnimator(transit, enter, nextAnim)
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
        hideBtmNav(activity)
    }

    private fun initToolbar() {
        if (post.isNotice()) {
            binding.txtTbTitle.text = TITLE_NOTICE
        } else {
            binding.txtTbTitle.text = TITLE_POST
        }
    }

    private fun initRecyclerView(activity: Activity) {
        binding.rvPostDetail.adapter =
            PostDetailAdapter(
                activity,
                glideProvider,
                postOverflowListenerInitializer =
                    object : ItemListenerInitializer<PostModel> {
                        override fun initialize(item: PostModel) {}

                        override fun initialize(
                            item: PostModel,
                            view: View?,
                        ) {
                            initPostOverflowMenuAndShow(activity, item, view!!)
                        }
                    },
                commentOverflowListenerInitializer =
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
        binding.rvPostDetail.layoutManager = LinearLayoutManager(activity)
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
        item: PostModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.edit_post -> {
                sendUserToEditPostScreen(circleId, item)
            }
            R.id.delete_post -> {
                ContentDeleteAlertDialog(
                    activity,
                    if (item.isNotice()) MESSAGE_DELETE_NOTICE else MESSAGE_DELETE_POST,
                ) {
                    viewModel.delete()
                }.show()
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
        bundle.putBoolean(Const.FLAG_IS_EDIT, true) // 수정 기능 전용 활성화
        findNavController().navigate(R.id.action_circleDetailPostDetailFragment_to_uploadPostFragment, bundle)
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
        viewModel.saveScrollState(binding.rvPostDetail.layoutManager?.onSaveInstanceState())
        when (it.itemId) {
            R.id.edit_comment -> {
                TextInputAlertDialog(
                    activity,
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
                ContentDeleteAlertDialog(activity, MESSAGE_DELETE_COMMENT) {
                    // 삭제 버튼 클릭 시
                    viewModel.deleteComment(comment.id)
                }.show()
            }
        }
        true
    }

    private fun showSoftInput(
        view: View,
        activity: Activity,
    ) {
        if (view.requestFocus()) {
            val imm = activity.getSystemService(InputMethodManager::class.java)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
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
        viewModel.uploadCommentState.observe(
            viewLifecycleOwner,
            uploadCommentStateObserver(activity),
        )
        viewModel.editCommentState.observe(
            viewLifecycleOwner,
            editCommentStateObserver(activity),
        )
        viewModel.deleteCommentState.observe(
            viewLifecycleOwner,
            deleteCommentStateObserver(activity),
        )
        viewModel.deletePostState.observe(
            viewLifecycleOwner,
            deletePostStateObserver(activity),
        )
        viewModel.scrollOver.observe(
            viewLifecycleOwner,
            scrollOverObserver(),
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

    private fun stateObserver(activity: Activity) =
        Observer<UiState> {
            val loadingIndicator = activity.findViewById<CircularProgressIndicator>(R.id.pgbLoading)
            loadingIndicator.isVisible = it is UiState.Loading
            when (it) {
                UiState.Success -> {
                    toggleView(binding.rvPostDetail)
                    loadContents()
                }
                UiState.AuthenticationError -> {
                    sendUserToLoginScreen(activity)
                    showErrorToast(activity)
                }
                UiState.ServiceError -> {
                    toggleView(binding.llServiceError)
                    showErrorDialog(activity)
                }
                else -> {}
            }
        }

    private fun showErrorToast(activity: Activity) {
        if (ErrorToast.previousFinished()) {
            ErrorToast(activity, viewModel.error).show()
        }
    }

    private fun showErrorDialog(activity: Activity) {
        ErrorAlertDialog(activity, viewModel.error).show()
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun loadContents() {
        binding.rvPostDetail.adapter?.let {
            (it as PostDetailAdapter).update(viewModel.contents) {
                viewModel.currentScrollState?.let {
                    binding.rvPostDetail.layoutManager?.onRestoreInstanceState(viewModel.currentScrollState)
                } ?: binding.rvPostDetail.scrollToPosition(0)
            }
        }
    }

    private fun uploadCommentStateObserver(activity: Activity) =
        Observer<UiState> {
            val loadingIndicator = activity.findViewById<CircularProgressIndicator>(R.id.pgbLoading)
            loadingIndicator.isVisible = it is UiState.Loading
            when (it) {
                UiState.Success -> {
                    requestRefreshToPreviousScreen()
                    loadContents()
                    hideSoftInput(activity, binding.edtComment)
                    binding.edtComment.text?.clear()
                }
                UiState.AuthenticationError -> {
                    sendUserToLoginScreen(activity)
                    showErrorToast(activity)
                }
                UiState.ServiceError -> {
                    showErrorDialog(activity)
                }
                else -> {}
            }
        }

    private fun editCommentStateObserver(activity: Activity) =
        Observer<UiState> {
            val loadingIndicator = activity.findViewById<CircularProgressIndicator>(R.id.pgbLoading)
            loadingIndicator.isVisible = it is UiState.Loading
            when (it) {
                UiState.Success -> {
                    loadContents()
                    hideSoftInput(activity, binding.edtComment)
                }
                UiState.AuthenticationError -> {
                    sendUserToLoginScreen(activity)
                    showErrorToast(activity)
                }
                UiState.ServiceError -> {
                    showErrorDialog(activity)
                }
                else -> {}
            }
        }

    private fun deleteCommentStateObserver(activity: Activity) =
        Observer<UiState> {
            val loadingIndicator = activity.findViewById<CircularProgressIndicator>(R.id.pgbLoading)
            loadingIndicator.isVisible = it is UiState.Loading
            when (it) {
                UiState.Success -> {
                    requestRefreshToPreviousScreen()
                    loadContents()
                }
                UiState.AuthenticationError -> {
                    sendUserToLoginScreen(activity)
                    showErrorToast(activity)
                }
                UiState.ServiceError -> {
                    ErrorAlertDialog(activity, viewModel.error).show()
                }
                else -> {}
            }
        }

    private fun hideSoftInput(
        activity: Activity,
        view: EditText,
    ) {
        val imm = activity.getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun deletePostStateObserver(activity: Activity) =
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
                    showErrorToast(activity)
                }
                UiState.ServiceError -> {
                    ErrorAlertDialog(activity, viewModel.error).show()
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

    private fun requestRefreshToPreviousScreen() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_CIRCLE_POST_DATA_CHANGED, true)
    }

    private fun initListener(activity: Activity) {
        setRvCircleCommentListener(activity)
        setBtnBackListener()
        setBtnRegisterCommentListener()
        setBtnRetryListener()
    }

    private fun setRvCircleCommentListener(activity: Activity) {
        binding.rvPostDetail.addOnScrollListener(viewModel.scrollListener)
        binding.rvPostDetail.addOnScrollListener(RecyclerViewHideSoftInputListener(activity))
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
