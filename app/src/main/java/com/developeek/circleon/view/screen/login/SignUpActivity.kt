package com.developeek.circleon.view.screen.login

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.developeek.circleon.databinding.ActivitySignUpBinding
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.adapter.SignUpFragmentAdapter
import com.developeek.circleon.view.viewmodelimpl.SignUpViewModelImpl
import com.developeek.circleon.view.widget.CustomAlertDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: SignUpViewModelImpl by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewPager(supportFragmentManager, lifecycle)
        initObserver(this)
        initListener()
    }

    private fun initViewPager(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) {
        binding.vpgSignUp.adapter = SignUpFragmentAdapter(fragmentManager, lifecycle)
        binding.vpgSignUp.isUserInputEnabled = false
    }

    private fun initObserver(activity: Activity) {
        viewModel.state.observe(
            activity as LifecycleOwner,
            stateObserver(activity),
        )
        viewModel.nameValidation.observe(
            activity as LifecycleOwner,
            nameValidationObserver(),
        )
        viewModel.emailValidation.observe(
            activity as LifecycleOwner,
            emailValidationObserver(),
        )
        viewModel.emailDuplication.observe(
            activity as LifecycleOwner,
            emailDuplicationObserver(),
        )
        viewModel.emailAuthenticationCodeRequest.observe(
            activity as LifecycleOwner,
            emailAuthenticationCodeRequestObserver(),
        )
    }

    private fun stateObserver(activity: Activity) =
        Observer<UiState> {
            when (it) {
                UiState.Error -> {
                    CustomAlertDialog(
                        activity,
                        viewModel.error,
                    ).show()
                }
                else -> {}
            }
        }

    private fun nameValidationObserver() =
        Observer<String> {
            binding.btnBottom.isClickable = it == SUCCESS
        }

    private fun emailValidationObserver() =
        Observer<String> {
            binding.btnBottom.isClickable = it == SUCCESS
        }

    private fun emailDuplicationObserver() =
        Observer<Boolean> {
            if (!it) {
                setBtnBottomAsEmailAuthenticateCodeButton()
            }
        }

    private fun emailAuthenticationCodeRequestObserver() =
        Observer<Boolean> {
            if (it) {
                setBtnBottomAsEmailAuthenticationButton()
            }
        }

    private fun initListener() {
        setBtnFinishListener()
        setVpgSignUpListener()
    }

    private fun setBtnFinishListener() {
        binding.btnFinish.setOnClickListener {
            finish()
        }
    }

    private fun setVpgSignUpListener() {
        binding.vpgSignUp.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    when (position) {
                        0 -> {
                            setBtnBottomAsNextButton()
                        }
                        1 -> {
                            setBtnBottomAsEmailCheckButton()
                        }
                        2 -> {
                            setBtnBottomAsSignUpButton()
                        }
                    }
                }
            },
        )
    }

    private fun setBtnBottomAsNextButton() {
        binding.btnBottom.setOnClickListener {
            binding.vpgSignUp.currentItem += 1
        }
        binding.btnBottom.text = BUTTON_NEXT
        binding.btnBottom.isClickable = false
    }

    private fun setBtnBottomAsEmailCheckButton() {
        binding.btnBottom.setOnClickListener {
            viewModel.checkEmailDuplication()
        }
        binding.btnBottom.text = BUTTON_CHECK_EMAIL_DUPLICATION
        binding.btnBottom.isClickable = false
    }

    private fun setBtnBottomAsEmailAuthenticateCodeButton() {
        binding.btnBottom.setOnClickListener {
            viewModel.requestEmailAuthenticationCode()
        }
        binding.btnBottom.text = BUTTON_REQUEST_AUTHENTICATION_CODE
    }

    private fun setBtnBottomAsEmailAuthenticationButton() {
        binding.btnBottom.setOnClickListener {
            viewModel.authenticateEmail()
        }
        binding.btnBottom.text = BUTTON_AUTHENTICATE_EMAIL
    }

    private fun setBtnBottomAsSignUpButton() {
        binding.btnBottom.setOnClickListener {
            viewModel.signUp()
        }
        binding.btnBottom.text = BUTTON_REQUEST_SIGN_UP
    }

    companion object {
        private const val SUCCESS = ""
        private const val BUTTON_NEXT = "다음"
        private const val BUTTON_CHECK_EMAIL_DUPLICATION = "중복 검사하기"
        private const val BUTTON_REQUEST_AUTHENTICATION_CODE = "인증 번호 요청하기"
        private const val BUTTON_AUTHENTICATE_EMAIL = "인증하기"
        private const val BUTTON_REQUEST_SIGN_UP = "회원 가입"
    }
}
