package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.databinding.FragmentCircleDetailNoticeBinding
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CircleNoticeAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.CircleDetailNoticeViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailNoticeViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailNoticeFragment(
    private val circleId: Int,
) : Fragment() {
    private lateinit var binding: FragmentCircleDetailNoticeBinding
    private val viewModel: CircleDetailNoticeViewModel by viewModels<CircleDetailNoticeViewModelImpl>()

    @Inject
    lateinit var glideProvider: GlideProvider

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

        viewModel.load(circleId)
        initObserver(requireActivity())
        initListener()
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
                    toggleView(binding.rvCircleNotice)
                    loadCircleNotices(activity)
                }
                UiState.RefreshExpiration -> {
                    sendUserToLoginScreen(activity)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
                UiState.Error -> {
                    toggleView(binding.llServiceError)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
            }
        }

    private fun loadCircleNotices(activity: Activity) {
        binding.rvCircleNotice.adapter =
            CircleNoticeAdapter(
                activity,
                glideProvider,
                object : ItemListenerInitializer<PostModel> {
                    override fun initialize(item: PostModel) {
                        // TODO: 게시글 상세 화면 진입
                    }
                },
            )
        binding.rvCircleNotice.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvCircleNotice.adapter?.let {
            (it as CircleNoticeAdapter).update(viewModel.notices) { binding.rvCircleNotice.scrollToPosition(0) }
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
                    (it as CircleNoticeAdapter).update(viewModel.notices) {}
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
            if (!viewModel.notices.isLastPage()) {
                addScrollLoadingItemAndLoad()
            }
        }
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvCircleNotice.adapter?.let {
            (it as CircleNoticeAdapter).update(viewModel.notices.add(PostModel.emptyInstance())) {}
        }
        viewModel.scrollOver(circleId)
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.load(circleId)
        }
    }

    private fun toggleView(view: View) {
        binding.rvCircleNotice.visibility = visibleWhenTrue(view == binding.rvCircleNotice)
        binding.pgbLoading.visibility = visibleWhenTrue(view == binding.pgbLoading)
        binding.llServiceError.visibility = visibleWhenTrue(view == binding.llServiceError)
    }

    private fun visibleWhenTrue(state: Boolean) = if (state) View.VISIBLE else View.GONE
}
