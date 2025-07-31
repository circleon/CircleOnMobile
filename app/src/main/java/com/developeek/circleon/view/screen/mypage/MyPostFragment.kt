package com.developeek.circleon.view.screen.mypage

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.developeek.circleon.databinding.FragmentMyPostBinding
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.model.MyPostModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.adapter.MyPostAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.base.BaseFragment
import com.developeek.circleon.view.viewmodel.mypage.MyPostViewModel
import com.developeek.circleon.view.viewmodelimpl.mypage.MyPostScreen
import com.developeek.circleon.view.viewmodelimpl.mypage.MyPostViewModelImpl
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPostFragment : BaseFragment() {
    private val binding: FragmentMyPostBinding by lazy {
        FragmentMyPostBinding.inflate(layoutInflater)
    }
    private val viewModel: MyPostViewModel by viewModels<MyPostViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<MyPostViewModelImpl.MyPostViewModelFactory> {
                    it.create(isMyPosts)
                }
        },
    )
    private var isMyPosts = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isMyPosts = it.getBoolean(Const.TAGE_MY_POSTS)
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
        handleEventFlow(requireContext())
    }

    private fun initView(context: Context) {
        initToolbar(context)
        initRecyclerView(context)
    }

    private fun initToolbar(context: Context) {
        binding.txtTbTitle.text =
            if (isMyPosts) {
                context.getString(R.string.title_my_post)
            } else {
                context.getString(R.string.title_my_comment_post)
            }
    }

    private fun initRecyclerView(context: Context) {
        binding.rvMyPost.adapter =
            MyPostAdapter(
                itemListenerInitializer =
                    object : ItemListenerInitializer<MyPostModel> {
                        override fun initialize(item: MyPostModel) {
                            sendUserToPostDetailScreen(item)
                        }

                        override fun initialize(
                            item: MyPostModel,
                            view: View?,
                        ) {
                        }
                    },
            )
        binding.rvMyPost.layoutManager = LinearLayoutManager(context)
        binding.rvMyPost.itemAnimator = null
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvMyPost.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun sendUserToPostDetailScreen(item: MyPostModel) {
        findNavController().currentDestination?.let { // fragment 진입 도중 중복 navigate 방지
            if (it.id != R.id.circleDetailPostDetailFragment) {
                findNavController()
                    .navigate(
                        R.id.action_myPostFragment_to_circleDetailPostDetailFragment2,
                        bundleOf(
                            Pair(Const.TAG_CIRCLE_ID, item.circleId),
                            Pair(Const.TAG_CIRCLE_POST, item.post),
                        ),
                    )
            }
        }
    }

    private fun initListener() {
        setBtnBackListener()
        setBtnRetryListener()
        setRvMyPostListener()
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun setRvMyPostListener() {
        val scrollListener =
            RecyclerViewInfiniteScrollListener().apply {
                setScrollEndListener {
                    if (!viewModel.isLastPage) {
                        addScrollLoadingItemAndLoad()
                    }
                }
            }

        binding.rvMyPost.addOnScrollListener(scrollListener)
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvMyPost.adapter?.let {
            (it as MyPostAdapter).addLoadingItem()
            viewModel.scrollOver()
        }
    }

    private fun handleEventFlow(context: Context) {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        super.handleEvent(it)
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

    private fun handleScreenFlow(
        screenFlow: MyPostScreen,
        context: Context,
    ) {
        when (screenFlow) {
            is MyPostScreen.Success -> showSuccessView(screenFlow, context)
            is MyPostScreen.Loading -> showLoadingView()
            is MyPostScreen.Error -> showErrorView()
        }
    }

    private fun showSuccessView(
        screenFlow: MyPostScreen.Success,
        context: Context,
    ) {
        if (screenFlow.posts.isEmpty()) {
            switchView(binding.llNoResult)
            if (isMyPosts) {
                binding.txtNoResult.text = context.getString(R.string.message_no_my_posts)
            } else {
                binding.txtNoResult.text = context.getString(R.string.message_no_my_comment)
            }
            return
        }

        switchView(binding.rvMyPost)
        loadMyPostsAndDoAfter(
            screenFlow.posts,
            after = {},
        )
    }

    private fun loadMyPostsAndDoAfter(
        myPosts: Models<MyPostModel>,
        after: () -> Unit,
    ) {
        binding.rvMyPost.adapter?.let {
            (it as MyPostAdapter).update(myPosts) {
                after()
            }
        }
    }

    private fun showLoadingView() {
        switchView(binding.pgbLoading)
    }

    private fun showErrorView() {
        switchView(binding.llServiceError)
    }

    private fun switchView(view: View) {
        binding.rvMyPost.isVisible = view == binding.rvMyPost
        binding.pgbLoading.isVisible = view == binding.pgbLoading
        binding.llNoResult.isVisible = view == binding.llNoResult
        binding.llServiceError.isVisible = view == binding.llServiceError
    }
}
