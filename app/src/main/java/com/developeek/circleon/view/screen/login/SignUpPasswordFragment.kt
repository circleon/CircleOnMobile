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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.developeek.circleon.databinding.FragmentSignUpPasswordBinding
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.viewmodelimpl.SignUpViewModelImpl

class SignUpPasswordFragment : Fragment() {
    private lateinit var binding: FragmentSignUpPasswordBinding
    private val viewModel: SignUpViewModelImpl by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUpPasswordBinding.inflate(layoutInflater)

        initObserver(requireActivity())
        initListener()

        return binding.root
    }

    private fun initObserver(activity: Activity) {
        viewModel.validation.observe(
            activity as LifecycleOwner,
            validationObserver(),
        )
    }

    private fun validationObserver() =
        Observer<String> {
            if (binding.edtPassword.hasFocus()) {
                binding.txtPasswordValidation.text = it
                if (it == PASSWORD_VALIDATED) binding.txtPasswordValidation.text = Const.EMPTY_TEXT
            }
            if (binding.edtPasswordCheck.hasFocus()) {
                binding.txtPasswordCheckValidation.text = it
                if (it == PASSWORD_CHECK_VALIDATED) binding.txtPasswordCheckValidation.text = Const.EMPTY_TEXT
            }
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
            viewModel.setPasswordCheck(text.toString())
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
        private const val PASSWORD_VALIDATED = "3"
        private const val PASSWORD_CHECK_VALIDATED = "4"
    }
}
