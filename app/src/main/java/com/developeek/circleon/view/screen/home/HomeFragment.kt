package com.developeek.circleon.view.screen.home

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
import com.developeek.circleon.databinding.FragmentHomeBinding
import com.developeek.circleon.domain.model.CategoryModel
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.model.UserModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CategoryAdapter
import com.developeek.circleon.view.adapter.CircleAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.base.BaseFragment
import com.developeek.circleon.view.viewmodel.home.HomeViewModel
import com.developeek.circleon.view.viewmodelimpl.home.HomeScreen
import com.developeek.circleon.view.viewmodelimpl.home.HomeViewModelImpl
import com.developeek.circleon.view.widget.SingleMessageToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    private val viewModel: HomeViewModel by viewModels<HomeViewModelImpl>()

    @Inject
    lateinit var glideProvider: GlideProvider

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
        initListener(requireContext())
        handleEventFlow()
    }

    private fun initView(context: Context) {
        initCategoryRecyclerView(context)
        initCircleRecyclerView(context)
        loadUserInfo(context, viewModel.user)
    }

    private fun initCategoryRecyclerView(context: Context) {
        binding.rvCircleCategory.adapter =
            CategoryAdapter(
                context,
                object : ItemListenerInitializer<CategoryModel> {
                    override fun initialize(item: CategoryModel) {
                        if (item.isSelected) {
                            binding.rvCircle.scrollToPosition(0)
                        } else {
                            viewModel.setFilterAndFetch(item.category)
                        }
                    }

                    override fun initialize(
                        item: CategoryModel,
                        view: View?,
                    ) {}
                },
            )
        binding.rvCircleCategory.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCircleCategory.itemAnimator = null
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvCircleCategory.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun initCircleRecyclerView(context: Context) {
        binding.rvCircle.adapter =
            CircleAdapter(
                context,
                glideProvider,
                object : ItemListenerInitializer<CircleModel> {
                    override fun initialize(item: CircleModel) {
                        findNavController()
                            .navigate(
                                R.id.action_homeFragment_to_circleDetailFragment,
                                bundleOf(
                                    Pair(Const.TAG_CIRCLE_ID, item.circleId),
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
        binding.rvCircle.layoutManager = LinearLayoutManager(context)
        binding.rvCircle.itemAnimator = null
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvCircle.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun loadUserInfo(
        context: Context,
        user: UserModel,
    ) {
        binding.txtUnivName.text = user.univ.univName()
        binding.txtContentTitleCircle.text =
            String.format(
                context.getString(R.string.home_content_title_circle), user.name,
            )
        user.profileImage?.let {
            glideProvider.fetchImage(it, context, binding.imgUserProfile)
        } ?: binding.imgUserProfile.setImageResource(R.drawable.ic_profile)
    }

    private fun initListener(context: Context) {
        setBtnSearchCircleListener()
        setBtnRetryListener()
        setBtnNotificationListener(context)
        setRvCircleListener()
    }

    private fun setBtnSearchCircleListener() {
        binding.btnSearch.setOnClickListener {
            sendUserToSearchCircleScreen()
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun setBtnNotificationListener(context: Context) {
        binding.btnNotification.setOnClickListener {
            if (SingleMessageToast.previousFinished()) {
                SingleMessageToast(
                    context,
                    context.getString(R.string.message_not_released),
                ).show()
            }
        }
    }

    private fun setRvCircleListener() {
        val scrollListener =
            RecyclerViewInfiniteScrollListener().apply {
                setScrollEndListener {
                    if (!viewModel.isLastPage) {
                        addScrollLoadingItemAndLoad()
                    }
                }
            }

        binding.rvCircle.addOnScrollListener(scrollListener)
    }

    private fun addScrollLoadingItemAndLoad() {
        binding.rvCircle.adapter?.let {
            (it as CircleAdapter).addLoadingItem()
            viewModel.scrollOver()
        }
    }

    private fun sendUserToSearchCircleScreen() {
        findNavController().navigate(R.id.action_homeFragment_to_searchCircleFragment)
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

    private fun handleScreenFlow(screenFlow: HomeScreen) {
        loadCircleCategory(screenFlow.categories)
        if (screenFlow !is HomeScreen.Loading) stopLoadingView()
        when (screenFlow) {
            is HomeScreen.Success -> showSuccessView(screenFlow)
            is HomeScreen.Loading -> startLoadingView()
            is HomeScreen.Error -> showErrorView()
        }
    }

    private fun loadCircleCategory(categories: Models<CategoryModel>) {
        binding.rvCircleCategory.adapter?.let {
            (it as CategoryAdapter).update(categories) {}
        }
    }

    private fun showSuccessView(screenFlow: HomeScreen.Success) {
        if (screenFlow.circles.isEmpty()) {
            switchView(binding.txtNoCircle)
            return
        }

        switchView(binding.rvCircle)
        loadCirclesAndDoAfter(
            screenFlow.circles,
            after = {
                if (!screenFlow.hasCollected) {
                    binding.rvCircle.scrollToPosition(0)
                    screenFlow.notifyCollected()
                }
            },
        )
    }

    private fun loadCirclesAndDoAfter(
        circles: Models<CircleModel>,
        after: () -> Unit,
    ) {
        binding.rvCircle.adapter?.let {
            (it as CircleAdapter).update(circles) {
                after()
            }
        }
    }

    private fun startLoadingView() {
        switchView(binding.shimmerCircle)
        binding.shimmerCircle.startShimmer()
    }

    private fun stopLoadingView() {
        binding.shimmerCircle.stopShimmer()
    }

    private fun showErrorView() {
        switchView(binding.llServiceError)
    }

    private fun switchView(view: View) {
        binding.rvCircle.isVisible = view == binding.rvCircle
        binding.shimmerCircle.isVisible = view == binding.shimmerCircle
        binding.txtNoCircle.isVisible = view == binding.txtNoCircle
        binding.llServiceError.isVisible = view == binding.llServiceError
    }
}
