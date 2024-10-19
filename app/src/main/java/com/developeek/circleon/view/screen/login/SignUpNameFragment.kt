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
            validationObserver(),
        )
    }

    private fun validationObserver() =
        Observer<String> {
            if (binding.edtName.hasFocus()) {
                binding.txtNameValidation.text = it
                if (it == NAME_VALIDATED) binding.txtNameValidation.text = Const.EMPTY_TEXT
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
