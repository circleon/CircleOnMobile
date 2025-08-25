package com.developeek.circleon.view.screen.home

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentCircleDetailNoticeBinding
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CirclePostAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.base.BaseFragment
import com.developeek.circleon.view.viewmodel.home.CircleDetailPostViewModel
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailNoticeViewModelImpl
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostScreen
import com.developeek.circleon.view.widget.CircleRequestAlertDialog
import com.developeek.circleon.view.widget.PositiveAlertDialog
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailNoticeFragment : BaseFragment() {
    private val binding: FragmentCircleDetailNoticeBinding by lazy {
        FragmentCircleDetailNoticeBinding.inflate(layoutInflater)
    }
    private val viewModel: CircleDetailPostViewModel by viewModels<CircleDetailNoticeViewModelImpl>(
        ownerProducer = {
            requireParentFragment()
        },
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<CircleDetailNoticeViewModelImpl.CircleDetailNoticeViewModelFactory> {
                    it.create(circleDetail)
                }
        },
    )
    private lateinit var circleDetail: CircleDetailModel

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circleDetail = it.getSerializable(Const.TAG_CIRCLE_DETAIL) as CircleDetailModel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireContext())
        initListener()
        initRefreshObserver()
        handleEventFlow()
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
                            showPopupMenuByUser(context, item, view!!)
                        }
                    },
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
                            Pair(Const.TAG_CIRCLE_ID, circleDetail.circleId),
                            Pair(Const.TAG_CIRCLE_POST, item),
                        ),
                    )
            }
        }
    }

    private fun showPopupMenuByUser(
        context: Context,
        item: PostModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(context, view) {}

        inflatePopupByUser(context, popupMenu, circleDetail.role, item)
        popupMenu.setOnMenuItemClickListener(noticeOverflowMenuItemClickListener(context, item))
        popupMenu.show()
    }

    private fun inflatePopupByUser(
        context: Context,
        popupMenu: PopupMenu,
        role: Role,
        item: PostModel,
    ) {
        when (role) {
            Role.MEMBER -> {
                popupMenu.inflate(R.menu.menu_non_executive_notice_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.report_post),
                    ContextCompat.getColor(context, R.color.error),
                )
            }
            Role.EXECUTIVE, Role.PRESIDENT -> {
                if (viewModel.user.userId == item.author.authorId) {
                    if (item.isPinned) {
                        popupMenu.inflate(R.menu.menu_pinned_author_notice_settings)
                    } else {
                        popupMenu.inflate(R.menu.menu_author_notice_settings)
                    }
                    Utils.changeMenuItemTextColor(
                        popupMenu.menu.findItem(R.id.delete_post),
                        ContextCompat.getColor(context, R.color.error),
                    )
                } else {
                    if (item.isPinned) {
                        popupMenu.inflate(R.menu.menu_pinned_notice_settings)
                    } else {
                        popupMenu.inflate(R.menu.menu_executive_notice_settings)
                    }
                    Utils.changeMenuItemTextColor(
                        popupMenu.menu.findItem(R.id.report_post),
                        ContextCompat.getColor(context, R.color.error),
                    )
                }
            }
            else -> {}
        }
    }

    private fun noticeOverflowMenuItemClickListener(
        context: Context,
        notice: PostModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.pin_post, R.id.unpin_post -> {
                viewModel.togglePin(notice)
            }
            R.id.edit_post -> {
                sendUserToEditNoticeScreen(circleDetail.circleId, notice)
            }
            R.id.delete_post -> {
                showDeleteNoticeRequestDialog(context, notice)
            }
            R.id.report_post -> {
                showReportNoticeRequestDialog(context, notice)
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

    private fun showDeleteNoticeRequestDialog(
        context: Context,
        notice: PostModel,
    ) {
        PositiveAlertDialog(
            context,
            context.getString(R.string.message_delete_notice),
            context.getString(R.string.btn_delete),
            positiveListener = {
                viewModel.delete(notice)
            },
        ).show()
    }

    private fun showReportNoticeRequestDialog(
        context: Context,
        notice: PostModel,
    ) {
        CircleRequestAlertDialog(
            context,
            title = context.getString(R.string.title_report_dialog),
            positiveButton = context.getString(R.string.menu_report),
            positiveListenerInitializer =
                object : ItemListenerInitializer<String> {
                    override fun initialize(item: String) {
                        viewModel.requestReportPost(notice.id, item)
                    }

                    override fun initialize(
                        item: String,
                        view: View?,
                    ) {}
                },
        ).show()
    }

    private fun initListener() {
        setRvCircleNoticeListener()
        setBtnRetryListener()
    }

    private fun setRvCircleNoticeListener() {
        val scrollListener =
            RecyclerViewInfiniteScrollListener().apply {
                setScrollEndListener {
                    if (!viewModel.isLastPage) {
                        addScrollLoadingItemAndLoad()
                    }
                }
            }

        binding.rvCircleNotice.addOnScrollListener(scrollListener)
        binding.swipeCircleNotice.setOnRefreshListener {
            viewModel.showLoadingAndRefresh()
        }
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvCircleNotice.adapter?.let {
            (it as CirclePostAdapter).addLoadingItem()
            viewModel.scrollOver()
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.showLoadingAndRefresh()
        }
    }

    private fun initRefreshObserver() {
        // 정보 수정 여부 감지
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.let {
                it.getLiveData<Boolean>(Const.FLAG_CIRCLE_POST_DATA_ADDED)
                    .observe(viewLifecycleOwner) { dataAdded ->
                        if (dataAdded) {
                            viewModel.refresh()
                            it[Const.FLAG_CIRCLE_POST_DATA_ADDED] = false
                        }
                    }
                it.getLiveData<Boolean>(Const.FLAG_CIRCLE_POST_DATA_CHANGED)
                    .observe(viewLifecycleOwner) { dataChanged ->
                        if (dataChanged) {
                            val post = it.get<PostModel>(Const.TAG_POST)!!
                            val newContent = it.get<String>(Const.TAG_POST_CONTENT)!!
                            viewModel.updatePostItem(post, newContent)
                            it[Const.FLAG_CIRCLE_POST_DATA_CHANGED] = false
                        }
                    }
                it.getLiveData<Boolean>(Const.FLAG_CIRCLE_POST_DATA_DELETED)
                    .observe(viewLifecycleOwner) { dataDeleted ->
                        if (dataDeleted) {
                            val post = it.get<PostModel>(Const.TAG_POST)!!
                            viewModel.deletePostItem(post)
                            it[Const.FLAG_CIRCLE_POST_DATA_DELETED] = false
                        }
                    }
            }
    }

    private fun handleEventFlow() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        super.handleEvent(it)
                    }
                }
                launch {
                    viewModel.screenFlow.collect {
                        handleScreenFlow(it)
                    }
                }
            }
        }
    }

    override fun showProcessing() {
        val loadingIndicator =
            requireParentFragment().requireView().findViewById<CircularProgressIndicator>(R.id.pgbLoading)

        loadingIndicator.isVisible = true
    }

    override fun endProcessing() {
        val loadingIndicator =
            requireParentFragment().requireView().findViewById<CircularProgressIndicator>(R.id.pgbLoading)

        loadingIndicator.isVisible = false
    }

    private fun handleScreenFlow(screenFlow: CircleDetailPostScreen) {
        if (screenFlow !is CircleDetailPostScreen.Loading) stopLoadingView()
        when (screenFlow) {
            is CircleDetailPostScreen.Success -> showSuccessView(screenFlow)
            is CircleDetailPostScreen.Loading -> startLoadingView()
            is CircleDetailPostScreen.Error -> showErrorView()
        }
    }

    private fun showSuccessView(screenFlow: CircleDetailPostScreen.Success) {
        if (screenFlow.posts.isEmpty()) {
            switchView(binding.txtNoNotice)
            return
        }

        switchView(binding.swipeCircleNotice)
        loadCircleNoticesAndDoAfter(screenFlow.posts) {
            if (screenFlow.hasCollected) {
                viewModel.currentScrollState?.let {
                    binding.rvCircleNotice.layoutManager?.onRestoreInstanceState(it)
                }
                return@loadCircleNoticesAndDoAfter
            }
            screenFlow.notifyCollected()
        }
    }

    private fun loadCircleNoticesAndDoAfter(
        notices: Models<PostModel>,
        after: () -> Unit,
    ) {
        binding.rvCircleNotice.adapter?.let {
            (it as CirclePostAdapter).update(notices) {
                after()
            }
        }
    }

    private fun startLoadingView() {
        switchView(binding.shimmerNotice)
        binding.shimmerNotice.startShimmer()
    }

    private fun stopLoadingView() {
        binding.shimmerNotice.stopShimmer()
        binding.swipeCircleNotice.isRefreshing = false
    }

    private fun showErrorView() {
        switchView(binding.llServiceError)
    }

    private fun switchView(view: View) {
        binding.swipeCircleNotice.isVisible = view == binding.swipeCircleNotice
        binding.shimmerNotice.isVisible = view == binding.shimmerNotice
        binding.txtNoNotice.isVisible = view == binding.txtNoNotice
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.saveScrollState(binding.rvCircleNotice.layoutManager?.onSaveInstanceState())
    }
}
