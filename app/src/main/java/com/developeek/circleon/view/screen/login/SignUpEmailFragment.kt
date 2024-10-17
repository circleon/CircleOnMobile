package com.developeek.circleon.view.screen.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.developeek.circleon.databinding.FragmentSignUpEmailBinding
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.viewmodelimpl.SignUpViewModelImpl

class SignUpEmailFragment : Fragment() {
    private lateinit var binding: FragmentSignUpEmailBinding
    private val viewModel: SignUpViewModelImpl by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUpEmailBinding.inflate(layoutInflater)

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
            if (binding.edtEmail.hasFocus()) {
                binding.txtEmailValidation.text = it
                if (it == EMAIL_VALIDATED) binding.txtEmailValidation.text = Const.EMPTY_TEXT
            }
            binding.llEmailAuthentication.isVisible = it == EMAIL_VALIDATED
        }

    private fun initListener() {
        initEdtEmailListener()
    }

    private fun initEdtEmailListener() {
        binding.edtEmail.doOnTextChanged { text, _, _, _ ->
            viewModel.setEmail(text.toString())
        }
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

    companion object {
        private const val EMAIL_VALIDATED = "2"
    }
}
