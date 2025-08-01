package com.developeek.circleon.view.screen.auth

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentChangePasswordEmailAuthenticationBinding
import com.developeek.circleon.view.viewmodel.auth.ChangePasswordViewModel
import com.developeek.circleon.view.viewmodelimpl.auth.ChangePasswordScreenEvent
import com.developeek.circleon.view.viewmodelimpl.auth.ChangePasswordStep
import com.developeek.circleon.view.viewmodelimpl.auth.ChangePasswordViewModelImpl
import kotlinx.coroutines.launch

class ChangePasswordEmailAuthenticationFragment : Fragment() {
    private val binding: FragmentChangePasswordEmailAuthenticationBinding by lazy {
        FragmentChangePasswordEmailAuthenticationBinding.inflate(layoutInflater)
    }
    private val viewModel: ChangePasswordViewModel by activityViewModels<ChangePasswordViewModelImpl>()

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

        initObserver()
        initListener()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.changePasswordScreenEvent.collect {
                    handleChangePasswordScreenEvent(it, requireContext())
                }
            }
        }
    }

    private fun initObserver() {
        viewModel.emailAuthenticationTimer.observe(
            viewLifecycleOwner,
            emailAuthenticationTimerObserver(),
        )
    }

    private fun emailAuthenticationTimerObserver() =
        Observer<Long> {
            val minute = it / MINUTE
            val second = (it % MINUTE) / SECOND
            binding.timerEmailAuthenticationCode.text = String.format(TIME_FORMAT, minute, second)
        }

    private fun initListener() {
        setBtnAuthenticateEmailListener()
        setBtnResendAuthenticationCodeListener()
    }

    private fun setBtnAuthenticateEmailListener() {
        binding.btnAuthenticateEmail.setOnClickListener {
            viewModel.authenticateEmail(binding.edtEmailAuthenticationCode.text.toString())
        }
    }

    private fun setBtnResendAuthenticationCodeListener() {
        binding.btnResendAuthenticationCode.setOnClickListener {
            viewModel.requestEmailCode()
        }
    }

    private fun handleChangePasswordScreenEvent(
        event: ChangePasswordScreenEvent,
        context: Context,
    ) {
        when (event) {
            is ChangePasswordScreenEvent.UpdateChangePasswordProcess -> updateChangePasswordProcess(event, context)
        }
    }

    private fun updateChangePasswordProcess(
        event: ChangePasswordScreenEvent.UpdateChangePasswordProcess,
        context: Context,
    ) {
        if (event.step != ChangePasswordStep.EMAIL_AUTHENTICATION) return

        loadValidationResult(event.stepCondition, context)
    }

    private fun loadValidationResult(
        stepCondition: Boolean,
        context: Context,
    ) {
        if (stepCondition) {
            changeAsValidatedView(context)
        } else {
            changeAsInvalidatedView(context)
        }
    }

    private fun changeAsValidatedView(context: Context) {
        binding.txtEmailAuthentication.isVisible = false
        binding.edtEmailAuthenticationCode.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.purple_5),
                    ContextCompat.getColor(context, R.color.grey_3),
                ),
            )
    }

    private fun changeAsInvalidatedView(context: Context) {
        binding.txtEmailAuthentication.isVisible = true
        binding.edtEmailAuthenticationCode.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.error),
                    ContextCompat.getColor(context, R.color.grey_3),
                ),
            )
    }

    override fun onResume() {
        super.onResume()

        binding.edtEmailAuthenticationCode.post {
            showSoftInput(binding.edtEmailAuthenticationCode, requireActivity())
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

    companion object {
        private const val TIME_FORMAT = "%02d:%02d"
        private const val MINUTE = 60000L
        private const val SECOND = 1000L
    }
}
