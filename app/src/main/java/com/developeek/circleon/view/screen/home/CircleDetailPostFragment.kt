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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentCircleDetailPostBinding
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.model.PostModels
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.adapter.CirclePostAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.home.CircleDetailPostViewModel
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostScreen
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostViewModelImpl
import com.developeek.circleon.view.widget.CircleRequestAlertDialog
import com.developeek.circleon.view.widget.PositiveAlertDialog
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailPostFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailPostBinding
    private lateinit var circleDetail: CircleDetailModel
    private val viewModel: CircleDetailPostViewModel by viewModels<CircleDetailPostViewModelImpl>(
        ownerProducer = {
            requireParentFragment()
        },
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<CircleDetailPostViewModelImpl.CircleDetailPostViewModelFactory> {
                    it.create(circleDetail)
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
            circleDetail = it.getSerializable(Const.TAG_CIRCLE_DETAIL) as CircleDetailModel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailPostBinding.inflate(layoutInflater)

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
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        handleEvent(it, requireActivity(), requireContext())
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

    private fun initView(context: Context) {
        initRecyclerView(context)
    }

    private fun initRecyclerView(context: Context) {
        binding.rvCirclePost.adapter =
            CirclePostAdapter(
                context,
                glideProvider,
                itemListenerInitializer =
                    object : ItemListenerInitializer<PostModel> {
                        override fun initialize(item: PostModel) {
                            sendUserToPostDetailFragment(item)
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
        binding.rvCirclePost.layoutManager = LinearLayoutManager(context)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvCirclePost.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun sendUserToPostDetailFragment(item: PostModel) {
        findNavController().currentDestination?.let { // fragment 진입 도중 중복 navigate 방지
            if (it.id != R.id.circleDetailPostDetailFragment) {
                findNavController()
                    .navigate(
                        R.id.action_circleDetailFragment_to_circleDetailPostDetailFragment,
                        bundleOf(
                            Pair(Const.TAG_CIRCLE_ID, circleDetail.id),
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

        inflatePopupByUser(context, popupMenu, item)
        popupMenu.setOnMenuItemClickListener(postOverflowMenuItemClickListener(context, item))
        popupMenu.show()
    }

    private fun inflatePopupByUser(
        context: Context,
        popupMenu: PopupMenu,
        item: PostModel,
    ) {
        userManager.getUser()?.let {
            if (it.id == item.author.id) { // 작성자 본인인 경우
                popupMenu.inflate(R.menu.menu_author_post_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.delete_post),
                    ContextCompat.getColor(context, R.color.error),
                )
            } else { // 작성자가 아닌 경우
                popupMenu.inflate(R.menu.menu_post_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.report_post),
                    ContextCompat.getColor(context, R.color.error),
                )
            }
        }
    }

    private fun postOverflowMenuItemClickListener(
        context: Context,
        postItem: PostModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.edit_post -> {
                sendUserToEditPostScreen(circleDetail.id, postItem)
            }

            R.id.delete_post -> {
                showDeletePostRequestDialog(context, postItem)
            }

            R.id.report_post -> {
                showReportPostRequestDialog(context, postItem)
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
        bundle.putSerializable(Const.TAG_POST_TYPE, PostType.POST)
        bundle.putSerializable(Const.TAG_CIRCLE_POST, item)
        bundle.putBoolean(Const.FLAG_EDIT_SCREEN, true) // 수정 기능 전용 활성화
        findNavController().navigate(R.id.action_circleDetailFragment_to_uploadPostFragment, bundle)
    }

    private fun showDeletePostRequestDialog(
        context: Context,
        post: PostModel,
    ) {
        PositiveAlertDialog(
            context,
            context.getString(R.string.message_delete_post),
            context.getString(R.string.btn_delete),
            positiveListener = {
                viewModel.deleteAndFetch(post.id)
            },
        ).show()
    }

    private fun showReportPostRequestDialog(
        context: Context,
        post: PostModel,
    ) {
        CircleRequestAlertDialog(
            context,
            title = context.getString(R.string.title_report_dialog),
            positiveButton = context.getString(R.string.menu_report),
            positiveListenerInitializer =
                object : ItemListenerInitializer<String> {
                    override fun initialize(item: String) {
                        viewModel.requestReportPost(post.id, item)
                    }

                    override fun initialize(
                        item: String,
                        view: View?,
                    ) {}
                },
        ).show()
    }

    private fun initListener() {
        setRvCirclePostListener()
        setBtnRetryListener()
    }

    private fun setRvCirclePostListener() {
        val scrollListener =
            RecyclerViewInfiniteScrollListener().apply {
                setScrollEndListener {
                    if (!viewModel.isLastPage) {
                        addScrollLoadingItemAndLoad()
                    }
                }
            }

        binding.rvCirclePost.addOnScrollListener(scrollListener)
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvCirclePost.adapter?.let {
            (it as CirclePostAdapter).addLoadingItem()
            viewModel.scrollOver()
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun initRefreshObserver() {
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

    private fun handleEvent(
        event: Event,
        parentActivity: Activity,
        context: Context,
    ) {
        val loadingIndicator =
            requireParentFragment().requireView().findViewById<CircularProgressIndicator>(R.id.pgbLoading)
        loadingIndicator.isVisible = event is Event.Loading
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

    private fun handleScreenFlow(screenFlow: CircleDetailPostScreen) {
        when (screenFlow) {
            is CircleDetailPostScreen.SuccessView -> showSuccessView(screenFlow)
            is CircleDetailPostScreen.LoadingView -> showLoadingView()
            is CircleDetailPostScreen.ErrorView -> showErrorView()
        }
    }

    private fun showSuccessView(screenFlow: CircleDetailPostScreen.SuccessView) {
        binding.shimmerPost.stopShimmer()
        if (screenFlow.posts.isEmpty()) {
            switchView(binding.txtNoPost)
            return
        }

        switchView(binding.rvCirclePost)
        loadCirclePostsAndDoAfter(screenFlow.posts) {
            if (screenFlow.hasCollected) {
                viewModel.currentScrollState?.let {
                    binding.rvCirclePost.layoutManager?.onRestoreInstanceState(it)
                }
                return@loadCirclePostsAndDoAfter
            }
            screenFlow.notifyCollected()
        }
    }

    private fun loadCirclePostsAndDoAfter(
        posts: PostModels,
        after: () -> Unit,
    ) {
        binding.rvCirclePost.adapter?.let {
            (it as CirclePostAdapter).update(posts) {
                after()
            }
        }
    }

    private fun showLoadingView() {
        switchView(binding.shimmerPost)
        binding.shimmerPost.startShimmer()
    }

    private fun showErrorView() {
        switchView(binding.llServiceError)
    }

    private fun switchView(view: View) {
        binding.rvCirclePost.isVisible = view == binding.rvCirclePost
        binding.shimmerPost.isVisible = view == binding.shimmerPost
        binding.txtNoPost.isVisible = view == binding.txtNoPost
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.saveScrollState(binding.rvCirclePost.layoutManager?.onSaveInstanceState())
    }
}
