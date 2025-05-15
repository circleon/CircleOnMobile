package com.developeek.circleon.view.screen.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.developeek.circleon.databinding.FragmentSignUpEmailAuthenticationBinding
import com.developeek.circleon.view.viewmodel.login.SignUpViewModel
import com.developeek.circleon.view.viewmodelimpl.login.SignUpViewModelImpl

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
        setEdtEmailCodeListener()
        setBtnResendAuthenticationCodeListener()
    }

    private fun setEdtEmailCodeListener() {
        binding.edtEmailAuthenticationCode.doOnTextChanged { text, _, _, _ ->
            viewModel.setEmailCode(text.toString())
        }
    }

    private fun setBtnResendAuthenticationCodeListener() {
        binding.btnResendAuthenticationCode.setOnClickListener {
            viewModel.requestEmailCode()
        }
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
