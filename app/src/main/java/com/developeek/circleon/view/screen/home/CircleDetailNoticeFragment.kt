package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
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
import com.developeek.circleon.view.viewmodel.CircleDetailPostViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailNoticeViewModelImpl
import com.developeek.circleon.view.widget.ContentDeleteAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
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

        initView(requireActivity())
        initObserver(requireActivity())
        initListener()
    }

    private fun initView(activity: Activity) {
        initRecyclerView(activity)
    }

    private fun initRecyclerView(activity: Activity) {
        binding.rvCircleNotice.adapter =
            CirclePostAdapter(
                activity,
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
                            initNoticeOverflowMenuAndShow(activity, item, view!!)
                        }
                    },
                role = role,
            )
        binding.rvCircleNotice.layoutManager = LinearLayoutManager(activity)
    }

    private fun sendUserToNoticeDetailScreen(item: PostModel) {
        findNavController()
            .navigate(
                R.id.action_circleDetailFragment_to_circleDetailPostDetailFragment,
                bundleOf(
                    Pair(Const.TAG_CIRCLE_ID, circleId),
                    Pair(Const.TAG_CIRCLE_POST, item),
                ),
            )
    }

    private fun initNoticeOverflowMenuAndShow(
        activity: Activity,
        item: PostModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(activity, view) {}
        if (item.isPinned) {
            popupMenu.inflate(R.menu.menu_pinned_notice_settings)
        } else {
            popupMenu.inflate(R.menu.menu_notice_settings)
        }

        popupMenu.setOnMenuItemClickListener(noticeOverflowMenuItemClickListener(activity, item))
        Utils.changeMenuItemTextColor(
            popupMenu.menu.findItem(R.id.delete_post),
            ContextCompat.getColor(activity, R.color.error),
        )

        popupMenu.show()
    }

    private fun noticeOverflowMenuItemClickListener(
        activity: Activity,
        item: PostModel,
    ) = PopupMenu.OnMenuItemClickListener {
        viewModel.saveScrollState(binding.rvCircleNotice.layoutManager?.onSaveInstanceState())
        when (it.itemId) {
            R.id.pin_post -> {
                if (item.isPinned) {
                    viewModel.removePinAndRefresh(item.id)
                } else {
                    viewModel.pinAndRefresh(item.id)
                }
            }

            R.id.edit_post -> {
                sendUserToEditNoticeScreen(circleId, item)
            }

            R.id.delete_post -> {
                ContentDeleteAlertDialog(activity, MESSAGE_DELETE_NOTICE) {
                    viewModel.deleteAndRefresh(item.id)
                }.show()
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
        bundle.putBoolean(Const.FLAG_EDIT_OR_NOT, true) // 수정 기능 전용 활성화
        findNavController().navigate(R.id.action_circleDetailFragment_to_uploadPostFragment, bundle)
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
        // 정보 수정 여부 감지
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
                        toggleView(binding.txtNoNotice)
                    } else {
                        toggleView(binding.rvCircleNotice)
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
        binding.pgbLoading.isVisible = view == binding.pgbLoading
        binding.txtNoNotice.isVisible = view == binding.txtNoNotice
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    companion object {
        private const val MESSAGE_DELETE_NOTICE = "공지사항을 삭제할까요?"
    }
}
