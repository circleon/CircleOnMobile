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
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentSignUpNameBinding
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.viewmodelimpl.SignUpViewModelImpl

class SignUpNameFragment : Fragment() {
    private lateinit var binding: FragmentSignUpNameBinding
    private val viewModel: SignUpViewModelImpl by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUpNameBinding.inflate(layoutInflater)

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
            if (binding.edtName.hasFocus()) {
                binding.txtNameValidation.text = it
                if (it == NAME_VALIDATED) {
                    whenNameValidated(activity)
                } else {
                    whenNameNotValidated(activity)
                }
            }
        }

    private fun whenNameValidated(activity: Activity) {
        binding.txtNameValidation.text = Const.EMPTY_TEXT
        binding.edtName.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(activity, R.color.purple_5),
                    ContextCompat.getColor(activity, R.color.grey_3),
                ),
            )
    }

    private fun whenNameNotValidated(activity: Activity) {
        binding.edtName.backgroundTintList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(
                    ContextCompat.getColor(activity, R.color.error),
                    ContextCompat.getColor(activity, R.color.grey_3),
                ),
            )
    }

    private fun initListener() {
        setEdtNameListener()
    }

    private fun setEdtNameListener() {
        binding.edtName.doOnTextChanged { text, _, _, _ ->
            viewModel.setName(text.toString())
        }
    }

    override fun onResume() {
        super.onResume()

        binding.edtName.post {
            showSoftInput(binding.edtName, requireActivity())
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
        private const val NAME_VALIDATED = "1"
    }
}
