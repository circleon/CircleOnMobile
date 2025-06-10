package com.developeek.circleon.view.screen.auth

import android.R
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
import com.developeek.circleon.databinding.FragmentSignUpEmailAuthenticationBinding
import com.developeek.circleon.view.viewmodel.auth.SignUpViewModel
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpScreenEvent
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpStep
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpViewModelImpl
import kotlinx.coroutines.launch

class SignUpEmailAuthenticationFragment : Fragment() {
    private lateinit var binding: FragmentSignUpEmailAuthenticationBinding
    private val viewModel: SignUpViewModel by activityViewModels<SignUpViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUpEmailAuthenticationBinding.inflate(layoutInflater)

        initObserver()
        initListener()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signUpScreenEvent.collect {
                    handleSignUpScreenEvent(it, requireContext())
                }
            }
        }

        return binding.root
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

    private fun handleSignUpScreenEvent(
        event: SignUpScreenEvent,
        context: Context,
    ) {
        when (event) {
            is SignUpScreenEvent.UpdateSignUpProcess -> updateSignUpProcess(event, context)
        }
    }

    private fun updateSignUpProcess(
        event: SignUpScreenEvent.UpdateSignUpProcess,
        context: Context,
    ) {
        if (event.step != SignUpStep.EMAIL_AUTHENTICATION) return

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
                arrayOf(intArrayOf(R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, com.developeek.circleon.R.color.purple_5),
                    ContextCompat.getColor(context, com.developeek.circleon.R.color.grey_3),
                ),
            )
    }

    private fun changeAsInvalidatedView(context: Context) {
        binding.txtEmailAuthentication.isVisible = true
        binding.edtEmailAuthenticationCode.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, com.developeek.circleon.R.color.error),
                    ContextCompat.getColor(context, com.developeek.circleon.R.color.grey_3),
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
