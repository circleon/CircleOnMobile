package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.databinding.ActivitySearchCircleBinding
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.adapter.CircleSearchResultAdapter
import com.developeek.circleon.view.listener.RecyclerViewHideSoftInputListener
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.SearchViewModel
import com.developeek.circleon.view.viewmodelimpl.SearchViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchCircleActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchCircleBinding
    private val viewModel: SearchViewModel by viewModels<SearchViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchCircleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView(this)
        initObserver(this)
        initListener(this)
    }

    private fun initView(activity: Activity) {
        binding.rvCircle.adapter = CircleSearchResultAdapter()
        binding.rvCircle.layoutManager = LinearLayoutManager(activity)
        binding.rvCircle.itemAnimator = null
        showSoftInput(binding.edtSearchCircle, activity)
    }

    private fun initObserver(activity: Activity) {
        viewModel.state.observe(
            activity as LifecycleOwner,
            stateObserver(activity),
        )
        viewModel.circles.observe(
            activity as LifecycleOwner,
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
            finish()
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadCircles()
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
