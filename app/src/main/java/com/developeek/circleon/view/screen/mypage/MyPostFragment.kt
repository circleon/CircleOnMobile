package com.developeek.circleon.view.screen.mypage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.developeek.circleon.databinding.FragmentMyPostBinding
import com.developeek.circleon.domain.model.MyPostModel
import com.developeek.circleon.domain.model.MyPostModels
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.adapter.MyPostAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.mypage.MyPostViewModel
import com.developeek.circleon.view.viewmodelimpl.mypage.MyPostScreen
import com.developeek.circleon.view.viewmodelimpl.mypage.MyPostViewModelImpl
import com.developeek.circleon.view.widget.SingleMessageToast
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPostFragment : Fragment() {
    private lateinit var binding: FragmentMyPostBinding
    private var isMyPosts = false
    private val viewModel: MyPostViewModel by viewModels<MyPostViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<MyPostViewModelImpl.MyPostViewModelFactory> {
                    it.create(isMyPosts)
                }
        },
    )

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
        binding = FragmentMyPostBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireContext())
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
                        handleScreenFlow(it)
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

    private fun handleEvent(
        event: Event,
        parentActivity: Activity,
        context: Context,
    ) {
        when (event) {
            is Event.SendToLoginScreen -> sendUserToLoginScreen(parentActivity)
            is Event.ShowToast -> showToast(event, context)
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

    private fun handleScreenFlow(screenFlow: MyPostScreen) {
        when (screenFlow) {
            is MyPostScreen.SuccessView -> showSuccessView(screenFlow)
            is MyPostScreen.LoadingView -> showLoadingView()
            is MyPostScreen.ErrorView -> showErrorView()
        }
    }

    private fun showSuccessView(screenFlow: MyPostScreen.SuccessView) {
        if (screenFlow.posts.isEmpty()) {
            switchView(binding.txtNoResult)
            return
        }

        switchView(binding.rvMyPost)
        loadMyPostsAndDoAfter(
            screenFlow.posts,
            after = {},
        )
    }

    private fun loadMyPostsAndDoAfter(
        myPosts: MyPostModels,
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
        binding.txtNoResult.isVisible = view == binding.txtNoResult
        binding.llServiceError.isVisible = view == binding.llServiceError
    }
}
