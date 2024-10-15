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
import com.developeek.circleon.databinding.FragmentSignUpNameBinding
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
        viewModel.nameValidation.observe(
            activity as LifecycleOwner,
            nameValidationObserver(activity),
        )
    }

    private fun nameValidationObserver(activity: Activity) =
        Observer<String> {
            if (it == SUCCESS) {
                binding.txtNameValidation.text = MESSAGE_SUCCESS
                binding.txtNameValidation.setTextColor(ContextCompat.getColor(activity, R.color.green_5))
            } else {
                binding.txtNameValidation.text = it
                binding.txtNameValidation.setTextColor(ContextCompat.getColor(activity, R.color.error))
            }
        }

    private fun initListener() {
        setEdtNameListener()
    }

    private fun setEdtNameListener() {
        binding.edtName.doOnTextChanged { text, _, _, _ ->
            viewModel.setName(text.toString())
        }
    }

    private fun initFocus(activity: Activity) {
        binding.edtName.post {
            showSoftInput(binding.edtName, activity)
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
