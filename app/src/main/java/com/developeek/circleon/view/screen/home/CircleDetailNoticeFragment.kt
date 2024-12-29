package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentCircleDetailNoticeBinding
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CirclePostAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.CircleDetailPostViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailNoticeViewModelImpl
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
        binding.rvCircleNotice.adapter =
            CirclePostAdapter(
                activity,
                glideProvider,
                object : ItemListenerInitializer<PostModel> {
                    override fun initialize(item: PostModel) {
                        findNavController()
                            .navigate(
                                R.id.action_circleDetailFragment_to_circleDetailPostDetailFragment,
                                bundleOf(
                                    Pair(Const.TAG_CIRCLE_POST, item),
                                ),
                            )
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
                        val popupMenu = object : PopupMenu(activity, view!!) {}
                        popupMenu.inflate(R.menu.menu_notice_settings)
                        popupMenu.setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.pin_post -> {
                                    if (item.isPinned) {
                                        viewModel.removePinAndLoad(item.id)
                                    } else {
                                        viewModel.pinAndLoad(item.id)
                                    }
                                }

                                R.id.modify_post -> {
                                    Toast.makeText(activity, "수정하기 ${item.id}", Toast.LENGTH_SHORT).show()
                                }

                                R.id.delete_post -> {
                                    Toast.makeText(activity, "삭제하기 ${item.id}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            true
                        }
                        popupMenu.show()
                    }
                },
                role = role,
            )
        binding.rvCircleNotice.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvCircleNotice.itemAnimator = null
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
            }
        }

    private fun loadCircleNotices() {
        binding.rvCircleNotice.adapter?.let {
            (it as CirclePostAdapter).update(viewModel.posts) {
                viewModel.currentScrollState?.let {
                    binding.rvCircleNotice.layoutManager?.onRestoreInstanceState(viewModel.currentScrollState)
                    viewModel.removeScrollState()
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
    }

    private fun setRvCircleNoticeListener() {
        binding.rvCircleNotice.addOnScrollListener(viewModel.scrollListener)
        (viewModel.scrollListener as RecyclerViewInfiniteScrollListener).setScrollEndListener {
            if (!viewModel.posts.isLastPage()) {
                addScrollLoadingItemAndLoad()
            }
        }
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvCircleNotice.adapter?.let {
            (it as CirclePostAdapter).update(viewModel.posts.add(PostModel.emptyInstance())) {}
        }
        viewModel.scrollOver(circleId)
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.load()
        }
    }

    override fun onStop() {
        super.onStop()

        viewModel.saveScrollState(binding.rvCircleNotice.layoutManager?.onSaveInstanceState())
    }

    private fun toggleView(view: View) {
        binding.rvCircleNotice.visibility = visibleWhenTrue(view == binding.rvCircleNotice)
        binding.pgbLoading.visibility = visibleWhenTrue(view == binding.pgbLoading)
        binding.txtNoNotice.visibility = visibleWhenTrue(view == binding.txtNoNotice)
        binding.llServiceError.visibility = visibleWhenTrue(view == binding.llServiceError)
    }

    private fun visibleWhenTrue(state: Boolean) = if (state) View.VISIBLE else View.GONE
}
