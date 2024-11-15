package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentHomeBinding
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CircleAdapter
import com.developeek.circleon.view.adapter.CircleCategoryAdapter
import com.developeek.circleon.view.listener.ItemClickListener
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
        initListener(requireActivity())
    }

    private fun initView(activity: Activity) {
        binding.rvCircle.adapter = CircleAdapter(activity, glideProvider)
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
                    // TODO: toggleLoadingScreen
                }
                UiState.Success -> {
                    // TODO: toggleCircleListScreen
                    loadCircles()
                    setRvCircleListener()
                }
                UiState.Error -> {
                    // TODO: toggleErrorScreen
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
                UiState.RefreshExpiration -> {
                    sendUserToLoginScreen(activity)
                }
            }
        }

    private fun loadCircles() {
        binding.rvCircle.adapter?.let {
            (it as CircleAdapter).update(viewModel.circles) { binding.rvCircle.scrollToPosition(0) }
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
                    object : ItemClickListener {
                        override fun onItemClicked(position: Int) {
                            if (viewModel.selectedCategory.value!!.isSame(viewModel.category[position])) {
                                binding.rvCircle.scrollToPosition(0)
                            } else {
                                viewModel.setFilterAndLoad(viewModel.category[position])
                            }
                        }
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
                binding.rvCircle.adapter?.let {
                    (it as CircleAdapter).update(viewModel.circles) {}
                    setRvCircleListener()
                }
            }
        }

    private fun setRvCircleListener() {
        binding.rvCircle.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                var completed = false

                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)

                    val manager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = manager.childCount
                    val totalItemCount = manager.itemCount
                    val firstItem = manager.findFirstVisibleItemPosition()

                    if (!completed && visibleItemCount + firstItem >= totalItemCount) {
                        viewModel.scrollOver()
                        completed = true
                    }
                }
            },
        )
    }

    private fun initListener(activity: Activity) {
        setBtnSearchCircleListener(activity)
    }

    private fun setBtnSearchCircleListener(activity: Activity) {
        binding.btnSearch.setOnClickListener {
            sendUserToSearchCircleScreen(activity)
        }
    }

    private fun sendUserToSearchCircleScreen(activity: Activity) {
        val intent = Intent(activity, SearchCircleActivity::class.java)

        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        viewModel.restore()
    }

    companion object {
        private const val CONTENT_TITLE_CIRCLE = "%s님 이런 동아리는 어떠신가요?"
    }
}
