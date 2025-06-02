package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentSearchCircleBinding
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.adapter.CircleSearchResultAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.RecyclerViewHideSoftInputListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.home.SearchCircleViewModel
import com.developeek.circleon.view.viewmodelimpl.home.SearchCircleScreen
import com.developeek.circleon.view.viewmodelimpl.home.SearchViewModelImpl
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchCircleFragment : Fragment() {
    private lateinit var binding: FragmentSearchCircleBinding
    private val viewModel: SearchCircleViewModel by viewModels<SearchViewModelImpl>()

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

        initView(requireActivity(), requireContext())
        initListener(requireContext())
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        handleEvent(it, requireActivity(), requireContext())
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

    private fun initView(
        parentActivity: Activity,
        context: Context,
    ) {
        initRecyclerView(context)
        initSoftKeyboard(context)
        hideBtmNav(parentActivity)
    }

    private fun initRecyclerView(context: Context) {
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
        binding.rvCircle.layoutManager = LinearLayoutManager(context)
        binding.rvCircle.itemAnimator = null
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvCircle.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
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
                    Pair(Const.TAG_CIRCLE_ID, item.circleId),
                    Pair(Const.TAG_CIRCLE_NAME, item.name),
                ),
                navOption,
            )
    }

    private fun initSoftKeyboard(context: Context) {
        showSoftInput(binding.edtSearchCircle, context)
    }

    private fun hideBtmNav(parentActivity: Activity) {
        parentActivity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    private fun initListener(context: Context) {
        setEdtSearchCircleListener()
        setRvCircleListener(context)
        setBtnClearListener(context)
        setBtnCancelListener()
        setBtnRetryListener()
    }

    private fun setEdtSearchCircleListener() {
        binding.edtSearchCircle.doOnTextChanged { text, _, _, _ ->
            viewModel.setKeywordAndFind(text.toString())
        }
    }

    private fun setRvCircleListener(context: Context) {
        binding.rvCircle.addOnScrollListener(RecyclerViewHideSoftInputListener(context))
    }

    private fun setBtnClearListener(context: Context) {
        binding.btnClear.setOnClickListener {
            viewModel.clearKeyword()
            binding.edtSearchCircle.setText(Const.EMPTY_TEXT)
            showSoftInput(binding.edtSearchCircle, context)
        }
    }

    private fun showSoftInput(
        view: View,
        context: Context,
    ) {
        if (view.requestFocus()) {
            val imm = context.getSystemService(InputMethodManager::class.java)
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

    private fun handleEvent(
        event: Event,
        parentActivity: Activity,
        context: Context,
    ) {
        when (event) {
            is Event.SendToLoginScreen -> sendUserToLoginScreen(parentActivity)
            is Event.ShowToast -> showToast(event, context)
            else -> {}
        }
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showToast(
        event: Event.ShowToast,
        context: Context,
    ) {
        if (SingleMessageToast.previousFinished()) {
            SingleMessageToast(context, event.message).show()
        }
    }

    private fun handleScreenFlow(screenFlow: SearchCircleScreen) {
        when (screenFlow) {
            is SearchCircleScreen.SuccessView -> showSuccessView(screenFlow)
            is SearchCircleScreen.LoadingView -> switchView(binding.pgbLoading)
            is SearchCircleScreen.ErrorView -> switchView(binding.llServiceError)
        }
    }

    private fun showSuccessView(screenFlow: SearchCircleScreen.SuccessView) {
        if (screenFlow.circles.isEmpty()) {
            switchView(binding.txtNoResult)
            return
        }

        loadCirclesAndDoAfter(
            screenFlow.circles,
            after = {
                switchView(binding.rvCircle)
            },
        )
    }

    private fun loadCirclesAndDoAfter(
        circles: Models<CircleSummaryModel>,
        after: () -> Unit,
    ) {
        (binding.rvCircle.adapter as CircleSearchResultAdapter).update(circles) {
            after()
        }
    }

    private fun switchView(view: View) {
        binding.rvCircle.isVisible = view == binding.rvCircle
        binding.pgbLoading.isVisible = view == binding.pgbLoading
        binding.txtNoResult.isVisible = view == binding.txtNoResult
        binding.llServiceError.isVisible = view == binding.llServiceError
    }
}
