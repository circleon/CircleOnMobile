package com.developeek.circleon.view.screen.login

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.developeek.circleon.R
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
        initListener(this)
    }

    private fun initViewPager(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) {
        binding.vpgSignUp.adapter = SignUpFragmentAdapter(fragmentManager, lifecycle)
        binding.vpgSignUp.isUserInputEnabled = false
        binding.vpgSignUp.offscreenPageLimit = 1
    }

    private fun initObserver(activity: Activity) {
        viewModel.state.observe(
            activity as LifecycleOwner,
            stateObserver(activity),
        )
        viewModel.validation.observe(
            activity as LifecycleOwner,
            validationObserver(activity),
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

    private fun validationObserver(activity: Activity) =
        Observer<String> {
            when (it) {
                NAME_VALIDATED -> {
                    binding.btnNext.isClickable = true
                    setBtnBottomAsNext()
                }
                EMAIL_VALIDATED -> {
                    binding.btnNext.isClickable = true
                    setBtnBottomAsRequestEmailCode()
                }
                EMAIL_CODE_REQUESTED -> {
                    binding.btnNext.isClickable = true
                    setBtnBottomAsAuthenticateEmail()
                }
                EMAIL_AUTHENTICATED -> {
                    binding.vpgSignUp.currentItem += 1
                }
                PASSWORD_VALIDATED, PASSWORD_CHECK_VALIDATED -> {
                    binding.btnNext.isClickable = true
                    setBtnBottomAsSignUp()
                }
                SIGN_UP_COMPLETED -> {
                    Toast.makeText(activity, "회원 가입 완료", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else -> {
                    if (binding.btnNext.isClickable) {
                        binding.btnNext.isClickable = false
                    }
                }
            }
        }

    private fun setBtnBottomAsNext() {
        binding.btnNext.setOnClickListener {
            binding.vpgSignUp.currentItem += 1
        }
    }

    private fun setBtnBottomAsRequestEmailCode() {
        binding.btnNext.setOnClickListener {
            viewModel.requestEmailCode()
            binding.vpgSignUp.currentItem += 1
        }
    }

    private fun setBtnBottomAsAuthenticateEmail() {
        binding.btnNext.setOnClickListener {
            viewModel.authenticateEmail()
        }
    }

    private fun setBtnBottomAsSignUp() {
        binding.btnNext.setOnClickListener {
            viewModel.signUp()
        }
    }

    private fun initListener(activity: Activity) {
        setVpgSignUpListener(activity)
        setBtnFinishListener()
    }

    private fun setVpgSignUpListener(activity: Activity) {
        binding.vpgSignUp.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.btnNext.isClickable = false
                    if (position == 3) {
                        binding.btnNext.text = ContextCompat.getString(activity, R.string.btn_sign_up)
                    }
                }
            },
        )
    }

    private fun setBtnFinishListener() {
        binding.btnFinish.setOnClickListener {
            finish()
        }
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?,
    ): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (binding.vpgSignUp.currentItem == 0) {
                super.onKeyDown(keyCode, event)
            } else {
                true
            }
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    companion object {
        const val NAME_VALIDATED = "1"
        const val EMAIL_VALIDATED = "2"
        const val EMAIL_CODE_REQUESTED = "3"
        const val EMAIL_AUTHENTICATED = "4"
        const val PASSWORD_VALIDATED = "5"
        const val PASSWORD_CHECK_VALIDATED = "6"
        const val SIGN_UP_COMPLETED = "7"
    }
}
