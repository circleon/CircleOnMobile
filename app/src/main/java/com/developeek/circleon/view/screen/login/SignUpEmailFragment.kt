package com.developeek.circleon.view.screen.login

import android.app.Activity
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.developeek.circleon.databinding.FragmentSignUpEmailBinding
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.viewmodel.SignUpViewModel
import com.developeek.circleon.view.viewmodelimpl.SignUpViewModelImpl

class SignUpEmailFragment : Fragment() {
    private lateinit var binding: FragmentSignUpEmailBinding
    private val viewModel: SignUpViewModel by activityViewModels<SignUpViewModelImpl>()

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
            validationObserver(activity),
        )
    }

    private fun validationObserver(activity: Activity) =
        Observer<String> {
            if (binding.edtEmail.hasFocus()) {
                binding.txtEmailValidation.text = it
                if (it == EMAIL_VALIDATED) {
                    whenEmailValidated(activity)
                } else {
                    whenEmailNotValidated(activity)
                }
            }
        }

    private fun whenEmailValidated(activity: Activity) {
        binding.txtEmailValidation.text = Const.EMPTY_TEXT
        binding.edtEmail.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(activity, com.developeek.circleon.R.color.purple_5),
                    ContextCompat.getColor(activity, com.developeek.circleon.R.color.grey_3),
                ),
            )
    }

    private fun whenEmailNotValidated(activity: Activity) {
        binding.edtEmail.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(activity, com.developeek.circleon.R.color.error),
                    ContextCompat.getColor(activity, com.developeek.circleon.R.color.grey_3),
                ),
            )
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
