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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels<HomeViewModelImpl>()

    @Inject
    lateinit var glideProvider: GlideProvider

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
    }

    private fun initView(activity: Activity) {
        binding.rvCircle.adapter = CircleAdapter(viewModel, activity, glideProvider)
        binding.rvCircle.layoutManager = LinearLayoutManager(activity)
        binding.rvCircle.itemAnimator = null
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
    }

    private fun stateObserver(activity: Activity) =
        Observer<UiState> {
            when (it) {
                UiState.Success -> {
                    loadCircles()
                }
                UiState.RefreshExpiration -> {
                    sendUserToLoginScreen(activity)
                }
                else -> {}
            }
        }

    private fun loadCircles() {
        binding.rvCircle.adapter?.let {
            (it as CircleAdapter).update { binding.rvCircle.scrollToPosition(0) }
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
                                binding.rvCircle.smoothScrollToPosition(0)
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

    override fun onStart() {
        super.onStart()
        viewModel.restore()
    }
}
