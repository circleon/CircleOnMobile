package com.developeek.circleon.view.screen.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentSignUpEmailBinding
import com.developeek.circleon.view.viewmodelimpl.SignUpViewModelImpl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpEmailFragment : Fragment() {
    private lateinit var binding: FragmentSignUpEmailBinding
    private val viewModel: SignUpViewModelImpl by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUpEmailBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initObserver(requireActivity())
        initListener()
        initFocus(requireActivity())
    }

    private fun initObserver(activity: Activity) {
        viewModel.emailValidation.observe(
            activity as LifecycleOwner,
            emailValidationObserver(activity),
        )

        viewModel.emailDuplication.observe(
            activity as LifecycleOwner,
            emailDuplicationObserver(activity),
        )
    }

    private fun emailValidationObserver(activity: Activity) =
        Observer<String> {
            if (it == SUCCESS) {
                binding.txtEmailValidation.text = SUCCESS
                binding.txtEmailValidation.setTextColor(ContextCompat.getColor(activity, R.color.green_5))
            } else {
                binding.txtEmailValidation.text = it
                binding.txtEmailValidation.setTextColor(ContextCompat.getColor(activity, R.color.error))
            }
        }

    private fun emailDuplicationObserver(activity: Activity) =
        Observer<Boolean> {
            if (!it) {
                binding.txtEmailValidation.text = MESSAGE_SUCCESS
                binding.txtEmailValidation.setTextColor(ContextCompat.getColor(activity, R.color.green_5))
                binding.llEmailAuthentication.visibility = View.VISIBLE
                binding.edtEmailAuthenticationCode.post {
                    showSoftInput(binding.edtEmailAuthenticationCode, activity)
                }
            }
        }

    private fun initListener() {
        initEdtEmailListener()
        initEdtEmailAuthenticationCodeListener()
    }

    private fun initEdtEmailListener() {
        binding.edtEmail.doOnTextChanged { text, _, _, _ ->
            viewModel.setEmail(text.toString())
        }
    }

    private fun initEdtEmailAuthenticationCodeListener() {
        binding.edtEmailAuthenticationCode.doOnTextChanged { text, _, _, _ ->
            viewModel.setEmailCode(text.toString())
        }
    }

    private fun initFocus(activity: Activity) {
        binding.edtEmail.post {
            showSoftInput(binding.edtEmail, activity)
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
        private const val SUCCESS = ""
        private const val MESSAGE_SUCCESS = "✓"
    }
}
