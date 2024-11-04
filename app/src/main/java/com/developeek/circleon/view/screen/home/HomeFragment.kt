package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.databinding.FragmentHomeBinding
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.view.adapter.CircleCategoryAdapter
import com.developeek.circleon.view.viewmodel.HomeViewModel
import com.developeek.circleon.view.viewmodelimpl.HomeViewModelImpl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels<HomeViewModelImpl>()

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

        initObserver(requireActivity())
    }

    private fun initObserver(activity: Activity) {
        viewModel.selectedCategory.observe(
            activity as LifecycleOwner,
            selectedCategoryObserver(activity),
        )
    }

    private fun selectedCategoryObserver(activity: Activity) =
        Observer<Category> {
            val scrollState = binding.rvCircleCategory.layoutManager?.onSaveInstanceState()
            binding.rvCircleCategory.adapter =
                CircleCategoryAdapter(
                    viewModel,
                    activity,
                )
            binding.rvCircleCategory.layoutManager =
                LinearLayoutManager(activity).also {
                    it.orientation = LinearLayoutManager.HORIZONTAL
                }
            binding.rvCircleCategory.layoutManager?.onRestoreInstanceState(scrollState)
        }
}
