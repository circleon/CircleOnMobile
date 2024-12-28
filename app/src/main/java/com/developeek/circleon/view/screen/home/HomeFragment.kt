package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentHomeBinding
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CircleAdapter
import com.developeek.circleon.view.adapter.CircleCategoryAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.HomeViewModel
import com.developeek.circleon.view.viewmodelimpl.HomeViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels<HomeViewModelImpl>()

    @Inject
    lateinit var glideProvider: GlideProvider

    @Inject
    lateinit var userManager: UserManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)

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
        binding.rvCircle.adapter =
            CircleAdapter(
                activity,
                glideProvider,
                object : ItemListenerInitializer<CircleModel> {
                    override fun initialize(item: CircleModel) {
                        findNavController()
                            .navigate(
                                R.id.action_homeFragment_to_circleDetailFragment,
                                bundleOf(
                                    Pair(Const.TAG_CIRCLE_ID, item.id),
                                    Pair(Const.TAG_CIRCLE_NAME, item.name),
                                ),
                            )
                    }

                    override fun initialize(
                        item: CircleModel,
                        view: View?,
                    ) {}
                },
            )
        binding.rvCircle.layoutManager = LinearLayoutManager(activity)
        binding.rvCircle.itemAnimator = null
        userManager.getUser()?.let {
            binding.txtUnivName.text = it.univ.univName()
            binding.txtContentTitleCircle.text = String.format(CONTENT_TITLE_CIRCLE, it.name)
        }
    }

    private fun initObserver(activity: Activity) {
        viewModel.state.observe(
            viewLifecycleOwner,
            stateObserver(activity),
        )
        viewModel.selectedCategory.observe(
            viewLifecycleOwner,
            selectedCategoryObserver(activity),
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
                    toggleView(binding.rvCircle)
                    loadCircles()
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

    private fun loadCircles() {
        binding.rvCircle.adapter?.let {
            (it as CircleAdapter).update(viewModel.circles) {
                viewModel.currentScrollState?.let {
                    binding.rvCircle.layoutManager?.onRestoreInstanceState(viewModel.currentScrollState)
                    viewModel.removeScrollState()
                } ?: binding.rvCircle.scrollToPosition(0)
            }
        }
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun selectedCategoryObserver(activity: Activity) =
        Observer<Category> {
            val scrollState = binding.rvCircleCategory.layoutManager?.onSaveInstanceState()
            binding.rvCircleCategory.adapter =
                CircleCategoryAdapter(
                    viewModel,
                    object : ItemListenerInitializer<Category> {
                        override fun initialize(item: Category) {
                            if (viewModel.selectedCategory.value!!.isSame(item)) {
                                binding.rvCircle.scrollToPosition(0)
                            } else {
                                viewModel.setFilterAndLoad(item)
                            }
                        }

                        override fun initialize(
                            item: Category,
                            view: View?,
                        ) {}
                    },
                    activity,
                )
            binding.rvCircleCategory.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            binding.rvCircleCategory.layoutManager?.onRestoreInstanceState(scrollState)
        }

    private fun scrollOverObserver() =
        Observer<Boolean> { completed ->
            if (completed) {
                binding.rvCircle.removeOnScrollListener(viewModel.scrollListener)
                binding.rvCircle.addOnScrollListener(viewModel.scrollListener) // 아이템 정보 업데이트
                binding.rvCircle.adapter?.let {
                    (it as CircleAdapter).update(viewModel.circles) {}
                }
            }
        }

    private fun initListener() {
        setBtnSearchCircleListener()
        setBtnRetryListener()
        setRvCircleListener()
    }

    private fun setBtnSearchCircleListener() {
        binding.btnSearch.setOnClickListener {
            sendUserToSearchCircleScreen()
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.restore()
        }
    }

    private fun setRvCircleListener() {
        binding.rvCircle.addOnScrollListener(viewModel.scrollListener)
        (viewModel.scrollListener as RecyclerViewInfiniteScrollListener).setScrollEndListener {
            if (!viewModel.circles.isLastPage()) {
                addScrollLoadingItemAndLoad()
            }
        }
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvCircle.adapter?.let {
            (it as CircleAdapter).update(viewModel.circles.add(CircleModel.emptyInstance())) {}
        }
        viewModel.scrollOver()
    }

    private fun sendUserToSearchCircleScreen() {
        findNavController().navigate(R.id.action_homeFragment_to_searchCircleFragment)
    }

    // 화면이 잠깐 보여지지 않는 경우가 아니라, 무조건 bottom tab 전환인 경우에만 scroll state 를 저장
    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.saveScrollState(binding.rvCircle.layoutManager?.onSaveInstanceState())
    }

    private fun toggleView(view: View) {
        binding.rvCircle.visibility = visibleWhenTrue(view == binding.rvCircle)
        binding.pgbLoading.visibility = visibleWhenTrue(view == binding.pgbLoading)
        binding.llServiceError.visibility = visibleWhenTrue(view == binding.llServiceError)
    }

    private fun visibleWhenTrue(state: Boolean) = if (state) View.VISIBLE else View.GONE

    companion object {
        private const val CONTENT_TITLE_CIRCLE = "%s 님 이런 동아리는 어떠신가요?"
    }
}
