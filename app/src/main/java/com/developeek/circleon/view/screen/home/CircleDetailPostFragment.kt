package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentCircleDetailPostBinding
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CirclePostAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.CircleDetailPostViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailPostViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
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

        initView(requireActivity())
        initObserver(requireActivity())
        initListener()
    }

    private fun initView(activity: Activity) {
        initRecyclerView(activity)
    }

    private fun initRecyclerView(activity: Activity) {
        binding.rvCirclePost.adapter =
            CirclePostAdapter(
                activity,
                glideProvider,
                object : ItemListenerInitializer<PostModel> {
                    override fun initialize(item: PostModel) {
                        sendUserToPostDetailFragment(item)
                    }

                    override fun initialize(
                        item: PostModel,
                        view: View?,
                    ) {}
                },
                object : ItemListenerInitializer<PostModel> {
                    override fun initialize(item: PostModel) {}

                    override fun initialize(
                        item: PostModel,
                        view: View?,
                    ) {
                        initPostOverflowMenuAndShow(activity, item, view!!)
                    }
                },
                userId = userManager.getUser()?.id,
            )
        binding.rvCirclePost.layoutManager = LinearLayoutManager(requireActivity())
    }

    private fun sendUserToPostDetailFragment(item: PostModel) {
        findNavController()
            .navigate(
                R.id.action_circleDetailFragment_to_circleDetailPostDetailFragment,
                bundleOf(
                    Pair(Const.TAG_CIRCLE_ID, circleId),
                    Pair(Const.TAG_CIRCLE_POST, item),
                    // 상세 화면 진입 시에만 anim 작동, bottom tab 전환 시에는 작동 X
                    Pair(Const.FLAG_ANIM_STATE, true),
                ),
            )
    }

    private fun initPostOverflowMenuAndShow(
        activity: Activity,
        item: PostModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(activity, view) {}
        popupMenu.inflate(R.menu.menu_post_settings)
        popupMenu.setOnMenuItemClickListener(postOverflowMenuItemClickListener(item))
        Utils.changeMenuItemTextColor(
            popupMenu.menu.findItem(R.id.delete_post),
            ContextCompat.getColor(activity, R.color.error),
        )

        popupMenu.show()
    }

    private fun postOverflowMenuItemClickListener(item: PostModel) =
        PopupMenu.OnMenuItemClickListener {
            when (it.itemId) {
                R.id.modify_post -> {
                    Toast.makeText(activity, "수정하기", Toast.LENGTH_SHORT).show()
                }
                R.id.delete_post -> {
                    viewModel.deleteAndRefresh(item.id)
                }
            }
            true
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
        // 댓글 정보 수정 여부 감지
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>(Const.FLAG_DATA_CHANGED)
            ?.observe(viewLifecycleOwner) {
                if (it) viewModel.refresh()
            }
    }

    private fun stateObserver(activity: Activity) =
        Observer<UiState> {
            when (it) {
                UiState.Loading -> {
                    toggleView(binding.pgbLoading)
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
        viewModel.scrollOver(circleId)
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
        binding.pgbLoading.isVisible = view == binding.pgbLoading
        binding.txtNoPost.isVisible = view == binding.txtNoPost
        binding.llServiceError.isVisible = view == binding.llServiceError
    }
}
