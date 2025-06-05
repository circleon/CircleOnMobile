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
import com.developeek.circleon.databinding.FragmentSignUpPasswordBinding
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.viewmodel.auth.SignUpViewModel
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpScreenEvent
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpStep
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpViewModelImpl
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SignUpPasswordFragment : Fragment() {
    private lateinit var binding: FragmentSignUpPasswordBinding
    private val viewModel: SignUpViewModel by activityViewModels<SignUpViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUpPasswordBinding.inflate(layoutInflater)

        initView()
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

    private fun initView() {
        loadPassword()
    }

    private fun loadPassword() {
        binding.edtPassword.setText(viewModel.signUpManager.passwordStringValue)
        binding.edtPasswordCheck.setText(viewModel.signUpManager.passwordCheckStringValue)
    }

    private fun initListener() {
        setEdtPasswordListener()
        setEdtPasswordCheckListener()
    }

    private fun setEdtPasswordListener() {
        binding.edtPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.setPassword(text.toString())
        }
    }

    private fun setEdtPasswordCheckListener() {
        binding.edtPasswordCheck.doOnTextChanged { text, _, _, _ ->
            viewModel.checkPassword(text.toString())
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
        if (event.step != SignUpStep.PASSWORD) return

        loadValidationResult(event.validationMessage, context)
    }

    private fun loadValidationResult(
        validationMessage: String,
        context: Context,
    ) {
        if (validationMessage == Const.EMPTY_TEXT || binding.edtPassword.text.toString().isEmpty()) {
            changePasswordAsValidatedView(context)
        } else {
            changePasswordAsInvalidatedView(context)
        }

        if (binding.edtPasswordCheck.text.toString() == binding.edtPassword.text.toString() ||
            binding.edtPasswordCheck.text.toString().isEmpty()
        ) {
            changePasswordCheckAsValidatedView(context)
        } else {
            changePasswordCheckAsInvalidatedView(context)
        }
    }

    private fun changePasswordAsValidatedView(context: Context) {
        binding.txtPasswordValidation.setTextColor(context.getColor(R.color.grey_6))
        binding.edtPassword.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.purple_5),
                    ContextCompat.getColor(context, R.color.grey_3),
                ),
            )
    }

    private fun changePasswordCheckAsValidatedView(context: Context) {
        binding.txtPasswordCheckValidation.isVisible = false
        binding.edtPasswordCheck.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.purple_5),
                    ContextCompat.getColor(context, R.color.grey_3),
                ),
            )
    }

    private fun changePasswordAsInvalidatedView(context: Context) {
        binding.txtPasswordValidation.setTextColor(context.getColor(R.color.error))
        binding.edtPassword.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.error),
                    ContextCompat.getColor(context, R.color.grey_3),
                ),
            )
    }

    private fun changePasswordCheckAsInvalidatedView(context: Context) {
        binding.txtPasswordCheckValidation.isVisible = true
        binding.edtPasswordCheck.backgroundTintList =
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

        binding.edtPassword.post {
            showSoftInput(binding.edtPassword, requireActivity())
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
