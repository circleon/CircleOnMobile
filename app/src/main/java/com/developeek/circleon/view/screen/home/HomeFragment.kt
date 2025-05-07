package com.developeek.circleon.view.screen.home

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
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentHomeBinding
import com.developeek.circleon.domain.model.CategoryModel
import com.developeek.circleon.domain.model.CategoryModels
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CategoryAdapter
import com.developeek.circleon.view.adapter.CircleAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewInfiniteScrollListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.home.HomeViewModel
import com.developeek.circleon.view.viewmodelimpl.home.HomeEvent
import com.developeek.circleon.view.viewmodelimpl.home.HomeViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels<HomeViewModelImpl>()

    @Inject
    lateinit var glideProvider: GlideProvider

    @Inject
    lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userManager.getUser() ?: sendUserToLoginScreen(requireActivity())
    }

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

        initView(requireContext())
        initListener()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect {
                    handleEvent(it, requireActivity(), requireContext())
                }
            }
        }
    }

    private fun initView(context: Context) {
        initCategoryRecyclerView(context)
        initCircleRecyclerView(context)
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
        binding.rvCircle.layoutManager = LinearLayoutManager(context)
        binding.rvCircle.itemAnimator = null
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvCircle.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun handleEvent(
        event: HomeEvent,
        parentActivity: Activity,
        context: Context,
    ) {
        loadCircleCategory(viewModel.categories)
        when (event) {
            is HomeEvent.ShowSuccessView -> showSuccessView(event)
            is HomeEvent.ShowInfiniteScrollSuccessView -> showInfiniteScrollSuccessView(event)
            is HomeEvent.ShowLoadingView -> showLoadingView()
            is HomeEvent.ShowErrorView -> showErrorView()
            is HomeEvent.SendToLoginScreen -> sendUserToLoginScreen(parentActivity)
            is HomeEvent.ShowToast -> showErrorToast(context, event.message)
        }
    }

    private fun loadCircleCategory(categories: CategoryModels) {
        binding.rvCircleCategory.adapter?.let {
            (it as CategoryAdapter).update(categories) {}
        }
    }

    private fun showSuccessView(event: HomeEvent.ShowSuccessView) {
        if (event.circles.isEmpty()) {
            toggleView(binding.txtNoCircle)
        } else {
            toggleView(binding.rvCircle)
            loadUserInfo()
            loadCircles(event.circles)
        }

        binding.shimmerCircle.stopShimmer()
    }

    private fun loadUserInfo() {
        userManager.getUser()?.let {
            binding.txtUnivName.text = it.univ.univName()
            binding.txtContentTitleCircle.text = String.format(CONTENT_TITLE_CIRCLE, it.name)
        }
    }

    private fun loadCircles(circles: CircleModels) {
        binding.rvCircle.adapter?.let {
            (it as CircleAdapter).update(circles) {
                viewModel.currentScrollState?.let {
                    binding.rvCircle.layoutManager?.onRestoreInstanceState(viewModel.currentScrollState)
                    viewModel.removeScrollState()
                } ?: binding.rvCircle.scrollToPosition(0)
            }
        }
    }

    private fun showInfiniteScrollSuccessView(event: HomeEvent.ShowInfiniteScrollSuccessView) {
        toggleView(binding.rvCircle)
        binding.rvCircle.adapter?.let {
            (it as CircleAdapter).update(event.circles) {}
        }
    }

    private fun showLoadingView() {
        toggleView(binding.shimmerCircle)
        binding.shimmerCircle.startShimmer()
    }

    private fun showErrorView() {
        toggleView(binding.llServiceError)
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showErrorToast(
        context: Context,
        message: String,
    ) {
        if (ErrorToast.previousFinished()) {
            ErrorToast(context, message).show()
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
            viewModel.refresh()
        }
    }

    private fun setRvCircleListener() {
        val scrollListener =
            RecyclerViewInfiniteScrollListener().apply {
                setScrollEndListener {
                    if (!viewModel.circles.isLastPage()) {
                        addScrollLoadingItemAndLoad()
                    }
                }
            }

        binding.rvCircle.addOnScrollListener(scrollListener)
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

    private fun toggleView(view: View) {
        binding.rvCircle.isVisible = view == binding.rvCircle
        binding.shimmerCircle.isVisible = view == binding.shimmerCircle
        binding.txtNoCircle.isVisible = view == binding.txtNoCircle
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    override fun onStop() {
        super.onStop()

        viewModel.saveScrollState(binding.rvCircle.layoutManager?.onSaveInstanceState())
    }

    companion object {
        private const val CONTENT_TITLE_CIRCLE = "%s 님 이런 동아리는 어떠신가요?"
    }
}
