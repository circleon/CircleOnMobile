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
import com.developeek.circleon.databinding.FragmentCircleDetailPostBinding
import com.developeek.circleon.domain.enums.PostType
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
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailPostViewModelImpl
import com.developeek.circleon.view.widget.ContentDeleteAlertDialog
import com.developeek.circleon.view.widget.ErrorAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailPostFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailPostBinding
    private var circleId = 0
    private val viewModel: CircleDetailPostViewModel by viewModels<CircleDetailPostViewModelImpl>(
        ownerProducer = {
            requireParentFragment()
        },
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<CircleDetailPostViewModelImpl.CircleDetailPostViewModelFactory> {
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
        initObserver(requireActivity(), requireContext())
        initListener()
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
                            initPostOverflowMenuAndShow(context, item, view!!)
                        }
                    },
                userId = userManager.getUser()?.id,
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
                            Pair(Const.TAG_CIRCLE_ID, circleId),
                            Pair(Const.TAG_CIRCLE_POST, item),
                        ),
                    )
            }
        }
    }

    private fun initPostOverflowMenuAndShow(
        context: Context,
        item: PostModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(context, view) {}
        popupMenu.inflate(R.menu.menu_post_settings)
        popupMenu.setOnMenuItemClickListener(postOverflowMenuItemClickListener(context, item))
        Utils.changeMenuItemTextColor(
            popupMenu.menu.findItem(R.id.delete_post),
            ContextCompat.getColor(context, R.color.error),
        )

        popupMenu.show()
    }

    private fun postOverflowMenuItemClickListener(
        context: Context,
        item: PostModel,
    ) = PopupMenu.OnMenuItemClickListener {
        viewModel.saveScrollState(binding.rvCirclePost.layoutManager?.onSaveInstanceState())
        when (it.itemId) {
            R.id.edit_post -> {
                sendUserToEditPostScreen(circleId, item)
            }
            R.id.delete_post -> {
                ContentDeleteAlertDialog(context, MESSAGE_DELETE_POST) {
                    viewModel.deleteAndFetch(item.id)
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
        bundle.putSerializable(Const.TAG_POST_TYPE, PostType.POST)
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
        if (it !is UiState.Loading) binding.shimmerPost.stopShimmer()
        when (it) {
            UiState.Loading -> {
                toggleView(binding.shimmerPost)
                binding.shimmerPost.startShimmer()
            }
            UiState.Success -> {
                if (viewModel.posts.isEmpty()) {
                    toggleView(binding.txtNoPost)
                } else {
                    toggleView(binding.rvCirclePost)
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
        binding.rvCirclePost.adapter?.let {
            (it as CirclePostAdapter).update(viewModel.posts) {
                viewModel.currentScrollState?.let {
                    binding.rvCirclePost.layoutManager?.onRestoreInstanceState(viewModel.currentScrollState)
                } ?: binding.rvCirclePost.scrollToPosition(0)
            }
        }
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showErrorToast(context: Context) {
        if (ErrorToast.previousFinished()) {
            ErrorToast(context, viewModel.error).show()
        }
    }

    private fun showErrorDialog(context: Context) {
        ErrorAlertDialog(context, viewModel.error).show()
    }

    private fun scrollOverObserver() =
        Observer<Boolean> { completed ->
            if (completed) {
                binding.rvCirclePost.removeOnScrollListener(viewModel.scrollListener)
                binding.rvCirclePost.addOnScrollListener(viewModel.scrollListener)
                binding.rvCirclePost.adapter?.let {
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
        setRvCirclePostListener()
        setBtnRetryListener()
    }

    private fun setRvCirclePostListener() {
        binding.rvCirclePost.addOnScrollListener(viewModel.scrollListener)
        (viewModel.scrollListener as RecyclerViewInfiniteScrollListener).setScrollEndListener {
            if (!viewModel.posts.isLastPage()) {
                addScrollLoadingItemAndLoad()
                viewModel.saveScrollState(binding.rvCirclePost.layoutManager?.onSaveInstanceState())
            }
        }
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvCirclePost.adapter?.let {
            (it as CirclePostAdapter).update(viewModel.posts.add(PostModel.emptyInstance())) {}
        }
        viewModel.scrollOver()
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.saveScrollState(binding.rvCirclePost.layoutManager?.onSaveInstanceState())
    }

    private fun toggleView(view: View) {
        binding.rvCirclePost.isVisible = view == binding.rvCirclePost
        binding.shimmerPost.isVisible = view == binding.shimmerPost
        binding.txtNoPost.isVisible = view == binding.txtNoPost
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    companion object {
        private const val MESSAGE_DELETE_POST = "게시글을 삭제할까요?"
    }
}
