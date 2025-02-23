package com.developeek.circleon.view.screen.login

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentSignUpPasswordBinding
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.viewmodel.SignUpViewModel
import com.developeek.circleon.view.viewmodelimpl.SignUpViewModelImpl

class SignUpPasswordFragment : Fragment() {
    private lateinit var binding: FragmentSignUpPasswordBinding
    private val viewModel: SignUpViewModel by activityViewModels<SignUpViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUpPasswordBinding.inflate(layoutInflater)

        initObserver(requireContext())
        initListener()

        return binding.root
    }

    private fun initObserver(context: Context) {
        viewModel.validation.observe(
            viewLifecycleOwner,
            validationObserver(context),
        )
    }

    private fun validationObserver(context: Context) =
        Observer<String> {
            if (binding.edtPassword.hasFocus()) {
                if (it == PASSWORD_VALIDATED) {
                    whenPasswordValidated(context)
                } else {
                    whenPasswordNotValidated(context)
                }
            }
            if (binding.edtPasswordCheck.hasFocus()) {
                binding.txtPasswordCheckValidation.text = it
                if (it == PASSWORD_CHECK_VALIDATED) {
                    whenPasswordCheckValidated(context)
                } else {
                    whenPasswordCheckNotValidated(context)
                }
            }
        }

    private fun whenPasswordValidated(context: Context) {
        binding.txtPasswordValidation.setTextColor(ContextCompat.getColor(context, R.color.grey_5))
        binding.edtPassword.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.purple_5),
                    ContextCompat.getColor(context, R.color.grey_3),
                ),
            )
    }

    private fun whenPasswordNotValidated(context: Context) {
        binding.txtPasswordValidation.setTextColor(ContextCompat.getColor(context, R.color.error))
        binding.edtPassword.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.error),
                    ContextCompat.getColor(context, R.color.grey_3),
                ),
            )
    }

    private fun whenPasswordCheckValidated(context: Context) {
        binding.txtPasswordCheckValidation.text = Const.EMPTY_TEXT
        binding.edtPasswordCheck.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.purple_5),
                    ContextCompat.getColor(context, R.color.grey_3),
                ),
            )
    }

    private fun whenPasswordCheckNotValidated(context: Context) {
        binding.edtPasswordCheck.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.error),
                    ContextCompat.getColor(context, R.color.grey_3),
                ),
            )
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
            viewModel.checkPassword(binding.edtPassword.text.toString(), text.toString())
        }
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

    companion object {
        private const val PASSWORD_VALIDATED = "5"
        private const val PASSWORD_CHECK_VALIDATED = "6"
    }
}
