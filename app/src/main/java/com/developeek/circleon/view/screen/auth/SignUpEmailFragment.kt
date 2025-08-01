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
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentSignUpEmailBinding
import com.developeek.circleon.view.viewmodel.auth.SignUpViewModel
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpScreenEvent
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpStep
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpViewModelImpl
import kotlinx.coroutines.launch

class SignUpEmailFragment : Fragment() {
    private val binding: FragmentSignUpEmailBinding by lazy {
        FragmentSignUpEmailBinding.inflate(layoutInflater)
    }
    private val viewModel: SignUpViewModel by activityViewModels<SignUpViewModelImpl>()

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

        initListener()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signUpScreenEvent.collect {
                    handleSignUpScreenEvent(it, requireContext())
                }
            }
        }
    }

    private fun initListener() {
        initEdtEmailListener()
    }

    private fun initEdtEmailListener() {
        binding.edtEmail.doOnTextChanged { text, _, _, _ ->
            viewModel.setEmail(text.toString())
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
        if (event.step != SignUpStep.EMAIL) return

        loadValidationResult(event.stepCondition, event.validationMessage, context)
    }

    private fun loadValidationResult(
        stepCondition: Boolean,
        validationMessage: String,
        context: Context,
    ) {
        if (stepCondition || binding.edtEmail.text.toString().isEmpty()) {
            changeAsValidatedView(context)
        } else {
            changeAsInvalidatedView(validationMessage, context)
        }
    }

    private fun changeAsValidatedView(context: Context) {
        binding.txtEmailValidation.isVisible = false
        binding.edtEmail.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.purple_5),
                    ContextCompat.getColor(context, R.color.grey_3),
                ),
            )
    }

    private fun changeAsInvalidatedView(
        validationMessage: String,
        context: Context,
    ) {
        binding.txtEmailValidation.text = validationMessage
        binding.txtEmailValidation.isVisible = true
        binding.edtEmail.backgroundTintList =
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

        binding.edtEmail.post {
            showSoftInput(binding.edtEmail, requireActivity())
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
}
