package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentCircleDetailNoticeBinding
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CirclePostAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.home.CircleDetailPostViewModel
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailNoticeViewModelImpl
import com.developeek.circleon.view.widget.CircleRequestAlertDialog
import com.developeek.circleon.view.widget.PositiveAlertDialog
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailNoticeFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailNoticeBinding
    private var circleId = 0
    private lateinit var role: Role
    private val viewModel: CircleDetailPostViewModel by viewModels<CircleDetailNoticeViewModelImpl>(
        ownerProducer = {
            requireParentFragment()
        },
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<CircleDetailNoticeViewModelImpl.CircleDetailNoticeViewModelFactory> {
                    it.create(circleId)
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
            role = it.getSerializable(Const.TAG_USER_ROLE) as Role
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailNoticeBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireContext())
        initObserver(requireActivity(), requireContext())
        initListener()
    }

    private fun initView(context: Context) {
        initRecyclerView(context)
    }

    private fun initRecyclerView(context: Context) {
        binding.rvCircleNotice.adapter =
            CirclePostAdapter(
                context,
                glideProvider,
                itemListenerInitializer =
                    object : ItemListenerInitializer<PostModel> {
                        override fun initialize(item: PostModel) {
                            sendUserToNoticeDetailScreen(item)
                        }

                        override fun initialize(
                            item: PostModel,
                            view: View?,
                        ) {}
                    },
                overflowListenerInitializer =
                    object : ItemListenerInitializer<PostModel> {
                        override fun initialize(item: PostModel) {}

                        override fun initialize(
                            item: PostModel,
                            view: View?,
                        ) {
                            initNoticeOverflowMenuAndShow(context, item, view!!)
                        }
                    },
                role = role,
            )
        binding.rvCircleNotice.layoutManager = LinearLayoutManager(context)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvCircleNotice.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun sendUserToNoticeDetailScreen(item: PostModel) {
        findNavController().currentDestination?.let { // fragment 진입 도중 중복 navigate 방지
            if (it.id != R.id.circleDetailPostDetailFragment) {
                findNavController()
                    .navigate(
                        R.id.action_circleDetailFragment_to_circleDetailPostDetailFragment,
                        bundleOf(
                            Pair(Const.TAG_CIRCLE_ID, circleId),
                            Pair(Const.TAG_CIRCLE_POST, item),
                        ),
                    )
            }
        }
    }

    private fun initNoticeOverflowMenuAndShow(
        context: Context,
        item: PostModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(context, view) {}
        val userId = userManager.getUser()?.id

        userId?.let {
            if (role.isExecutive() && it == item.author.id) { // 임원이면서 작성자 본인인 경우
                if (item.isPinned) {
                    popupMenu.inflate(R.menu.menu_pinned_author_notice_settings)
                } else {
                    popupMenu.inflate(R.menu.menu_author_notice_settings)
                }
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.delete_post),
                    ContextCompat.getColor(context, R.color.error),
                )
            } else if (role.isExecutive()) { // 임원인 경우
                if (item.isPinned) {
                    popupMenu.inflate(R.menu.menu_pinned_notice_settings)
                } else {
                    popupMenu.inflate(R.menu.menu_executive_notice_settings)
                }
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.report_post),
                    ContextCompat.getColor(context, R.color.error),
                )
            } else { // 부원인 경우
                popupMenu.inflate(R.menu.menu_non_executive_notice_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.report_post),
                    ContextCompat.getColor(context, R.color.error),
                )
            }
        }
        popupMenu.setOnMenuItemClickListener(noticeOverflowMenuItemClickListener(context, item))
        popupMenu.show()
    }

    private fun noticeOverflowMenuItemClickListener(
        context: Context,
        noticeItem: PostModel,
    ) = PopupMenu.OnMenuItemClickListener {
        viewModel.saveScrollState(binding.rvCircleNotice.layoutManager?.onSaveInstanceState())
        when (it.itemId) {
            R.id.pin_post -> {
                viewModel.pinAndFetch(noticeItem.id)
            }

            R.id.unpin_post -> {
                viewModel.removePinAndFetch(noticeItem.id)
            }

            R.id.edit_post -> {
                sendUserToEditNoticeScreen(circleId, noticeItem)
            }

            R.id.delete_post -> {
                PositiveAlertDialog(
                    context,
                    MESSAGE_DELETE_NOTICE,
                    ContextCompat.getString(context, R.string.btn_delete),
                    positiveListener = {
                        viewModel.deleteAndFetch(noticeItem.id)
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
                                viewModel.reportPost(noticeItem.id, item)
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

    private fun sendUserToEditNoticeScreen(
        circleId: Int,
        item: PostModel,
    ) {
        val bundle = Bundle()

        bundle.putInt(Const.TAG_CIRCLE_ID, circleId)
        bundle.putSerializable(Const.TAG_POST_TYPE, PostType.NOTICE)
        bundle.putSerializable(Const.TAG_CIRCLE_POST, item)
        bundle.putBoolean(Const.FLAG_EDIT_SCREEN, true) // 수정 기능 전용 활성화
        findNavController().navigate(R.id.action_circleDetailFragment_to_uploadPostFragment, bundle)
    }

    private fun initObserver(
        parentActivity: Activity,
        context: Context,
    ) {
        viewModel.state.observe(
            viewLifecycleOwner,
            stateObserver(parentActivity, context),
        )
        viewModel.scrollOver.observe(
            viewLifecycleOwner,
            scrollOverObserver(),
        )
        viewModel.postState.observe(
            viewLifecycleOwner,
            postStateObserver(parentActivity, context),
        )
        // 정보 수정 여부 감지
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.let {
                it.getLiveData<Boolean>(Const.FLAG_CIRCLE_POST_DATA_CHANGED)
                    .observe(viewLifecycleOwner) { dataChanged ->
                        if (dataChanged) {
                            viewModel.refresh()
                            it[Const.FLAG_CIRCLE_POST_DATA_CHANGED] = false
                        }
                    }
            }
    }

    private fun stateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        if (it !is UiState.Loading) binding.shimmerNotice.stopShimmer()
        when (it) {
            UiState.Loading -> {
                toggleView(binding.shimmerNotice)
                binding.shimmerNotice.startShimmer()
            }
            UiState.Success -> {
                if (viewModel.posts.isEmpty()) {
                    toggleView(binding.txtNoNotice)
                } else {
                    toggleView(binding.rvCircleNotice)
                    loadCircleNotices()
                }
            }
            UiState.AuthenticationError -> {
                sendUserToLoginScreen(parentActivity)
                showErrorToast(context)
            }
            UiState.ServiceError -> {
                toggleView(binding.llServiceError)
                showErrorToast(context)
            }
            else -> {}
        }
    }

    private fun loadCircleNotices() {
        binding.rvCircleNotice.adapter?.let {
            (it as CirclePostAdapter).update(viewModel.posts) {
                viewModel.currentScrollState?.let {
                    binding.rvCircleNotice.layoutManager?.onRestoreInstanceState(viewModel.currentScrollState)
                } ?: binding.rvCircleNotice.scrollToPosition(0)
            }
        }
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showErrorToast(context: Context) {
        if (SingleMessageToast.previousFinished()) {
            SingleMessageToast(context, viewModel.error).show()
        }
    }

    private fun showErrorDialog(context: Context) {
        SingleMessageAlertDialog(context, viewModel.error).show()
    }

    private fun scrollOverObserver() =
        Observer<Boolean> { completed ->
            if (completed) {
                binding.rvCircleNotice.removeOnScrollListener(viewModel.scrollListener)
                binding.rvCircleNotice.addOnScrollListener(viewModel.scrollListener)
                binding.rvCircleNotice.adapter?.let {
                    (it as CirclePostAdapter).update(viewModel.posts) {}
                }
            }
        }

    private fun postStateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        val loadingIndicator =
            requireParentFragment().requireView().findViewById<CircularProgressIndicator>(R.id.pgbLoading)
        loadingIndicator.isVisible = it is UiState.Loading
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

    private fun initListener() {
        setRvCircleNoticeListener()
        setBtnRetryListener()
        setFabRegisterNoticeListener()
    }

    private fun setRvCircleNoticeListener() {
        binding.rvCircleNotice.addOnScrollListener(viewModel.scrollListener)
        (viewModel.scrollListener as RecyclerViewInfiniteScrollListener).setScrollEndListener {
            if (!viewModel.posts.isLastPage()) {
                addScrollLoadingItemAndLoad()
                // scrollOver 시 문제가 발생하더라도 정상으로 돌아온 경우 스크롤 복원하기 위함
                viewModel.saveScrollState(binding.rvCircleNotice.layoutManager?.onSaveInstanceState())
            }
        }
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvCircleNotice.adapter?.let {
            (it as CirclePostAdapter).update(viewModel.posts.add(PostModel.emptyInstance())) {}
        }
        viewModel.scrollOver()
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun setFabRegisterNoticeListener() {
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.saveScrollState(binding.rvCircleNotice.layoutManager?.onSaveInstanceState())
    }

    private fun toggleView(view: View) {
        binding.rvCircleNotice.isVisible = view == binding.rvCircleNotice
        binding.shimmerNotice.isVisible = view == binding.shimmerNotice
        binding.txtNoNotice.isVisible = view == binding.txtNoNotice
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    companion object {
        private const val MESSAGE_DELETE_NOTICE = "공지사항을 삭제할까요?"
    }
}
