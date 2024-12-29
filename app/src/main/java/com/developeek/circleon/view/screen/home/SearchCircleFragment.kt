package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentSearchCircleBinding
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.adapter.CircleSearchResultAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewHideSoftInputListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.SearchViewModel
import com.developeek.circleon.view.viewmodelimpl.SearchViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchCircleFragment : Fragment() {
    private lateinit var binding: FragmentSearchCircleBinding
    private val viewModel: SearchViewModel by viewModels<SearchViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSearchCircleBinding.inflate(layoutInflater)

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
        initRecyclerView()
        initSoftKeyboard(activity)
    }

    private fun initRecyclerView() {
        binding.rvCircle.adapter =
            CircleSearchResultAdapter(
                object : ItemListenerInitializer<CircleSummaryModel> {
                    override fun initialize(item: CircleSummaryModel) {
                        sendUserToCircleDetailScreen(item)
                    }

                    override fun initialize(
                        item: CircleSummaryModel,
                        view: View?,
                    ) { }
                },
            )
        binding.rvCircle.layoutManager = LinearLayoutManager(activity)
        binding.rvCircle.itemAnimator = null
    }

    private fun sendUserToCircleDetailScreen(item: CircleSummaryModel) {
        // 상세 화면 이동 시 검색 화면은 백스택에서 제거
        val navOption =
            NavOptions
                .Builder()
                .setPopUpTo(
                    R.id.searchCircleFragment,
                    true,
                    false,
                )
                .build()

        findNavController()
            .navigate(
                R.id.action_searchCircleFragment_to_circleDetailFragment,
                bundleOf(
                    Pair(Const.TAG_CIRCLE_ID, item.id),
                    Pair(Const.TAG_CIRCLE_NAME, item.name),
                ),
                navOption,
            )
    }

    private fun initSoftKeyboard(activity: Activity) {
        showSoftInput(binding.edtSearchCircle, activity)
    }

    private fun initObserver(activity: Activity) {
        viewModel.state.observe(
            viewLifecycleOwner,
            stateObserver(activity),
        )
        viewModel.circles.observe(
            viewLifecycleOwner,
            circlesObserver(),
        )
    }

    private fun stateObserver(activity: Activity) =
        Observer<UiState> {
            when (it) {
                UiState.Loading -> {
                    toggleView(binding.pgbLoading)
                }
                UiState.Success -> {
                    viewModel.setKeywordAndFind(binding.edtSearchCircle.text.toString())
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

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun circlesObserver() =
        Observer<CircleSummaryModels> {
            when (it.isEmpty()) {
                true -> toggleView(binding.txtNoResult)
                false -> {
                    toggleView(binding.rvCircle)
                    (binding.rvCircle.adapter as CircleSearchResultAdapter).update(it) {}
                }
            }
        }

    private fun initListener(activity: Activity) {
        setEdtSearchCircleListener()
        setRvCircleListener(activity)
        setBtnClearListener(activity)
        setBtnCancelListener()
        setBtnRetryListener()
    }

    private fun setEdtSearchCircleListener() {
        binding.edtSearchCircle.doOnTextChanged { text, _, _, _ ->
            viewModel.setKeywordAndFind(text.toString())
        }
    }

    private fun setRvCircleListener(activity: Activity) {
        binding.rvCircle.addOnScrollListener(RecyclerViewHideSoftInputListener(activity))
    }

    private fun setBtnClearListener(activity: Activity) {
        binding.btnClear.setOnClickListener {
            viewModel.clearKeyword()
            binding.edtSearchCircle.setText(Const.EMPTY_TEXT)
            showSoftInput(binding.edtSearchCircle, activity)
        }
    }

    private fun showSoftInput(
        view: View,
        activity: Activity,
    ) {
        if (view.requestFocus()) {
            val imm = activity.getSystemService(InputMethodManager::class.java)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setBtnCancelListener() {
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun toggleView(view: View) {
        binding.rvCircle.visibility = visibleWhenTrue(view == binding.rvCircle)
        binding.pgbLoading.visibility = visibleWhenTrue(view == binding.pgbLoading)
        binding.txtNoResult.visibility = visibleWhenTrue(view == binding.txtNoResult)
        binding.llServiceError.visibility = visibleWhenTrue(view == binding.llServiceError)
    }

    private fun visibleWhenTrue(state: Boolean) = if (state) View.VISIBLE else View.GONE
}
