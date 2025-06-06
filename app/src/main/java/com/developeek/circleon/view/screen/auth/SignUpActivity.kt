package com.developeek.circleon.view.screen.auth

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ActivitySignUpBinding
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.adapter.SignUpFragmentAdapter
import com.developeek.circleon.view.viewmodel.auth.SignUpViewModel
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpScreen
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpScreenEvent
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpStep
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpViewModelImpl
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: SignUpViewModel by viewModels<SignUpViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewPager(supportFragmentManager, lifecycle)
        initListener()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        handleEvent(it, this@SignUpActivity)
                    }
                }
                launch {
                    viewModel.screenFlow.collect {
                        handleScreenFlow(it)
                    }
                }
                launch {
                    viewModel.signUpScreenEvent.collect {
                        handleSignUpScreenEvent(it, this@SignUpActivity)
                    }
                }
            }
        }
    }

    private fun initViewPager(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) {
        binding.vpgSignUp.adapter = SignUpFragmentAdapter(fragmentManager, lifecycle)
        binding.vpgSignUp.isUserInputEnabled = false
        binding.vpgSignUp.offscreenPageLimit = 1
    }

    private fun handleEvent(
        event: Event,
        context: Context,
    ) {
        if (event is Event.ShowProcessing) {
            switchView(binding.pgbProcessing)
        } else {
            switchView(binding.btnBottom)
        }
        when (event) {
            is Event.ShowToast -> showToast(event, context)
            is Event.ShowDialog -> showDialog(event, context)
            else -> {}
        }
    }

    private fun showToast(
        event: Event.ShowToast,
        context: Context,
    ) {
        if (SingleMessageToast.previousFinished()) {
            SingleMessageToast(context, event.message).show()
        }
    }

    private fun showDialog(
        event: Event.ShowDialog,
        context: Context,
    ) {
        SingleMessageAlertDialog(context, event.message).show()
    }

    private fun handleScreenFlow(screenFlow: SignUpScreen) {
        if (screenFlow is SignUpScreen.LoadingView) {
            switchView(binding.pgbProcessing)
        } else {
            switchView(binding.btnBottom)
        }
        when (screenFlow) {
            is SignUpScreen.SuccessView -> finish()
            else -> {}
        }
    }

    private fun handleSignUpScreenEvent(
        event: SignUpScreenEvent,
        context: Context,
    ) {
        when (event) {
            is SignUpScreenEvent.UpdateSignUpProcess -> updateSignUpProcess(event, context)
        }
    }

    private fun updateSignUpProcess(
        event: SignUpScreenEvent.UpdateSignUpProcess,
        context: Context,
    ) {
        event.let {
            binding.vpgSignUp.currentItem = it.step.getIndex()
            toggleBtnBottomByStepCondition(it.stepCondition, context)
            switchBtnBottomTextByStep(it.step, context)
            if (it.stepCondition) setBtnBottomListenerByCurrentStep(it.step)
        }
    }

    private fun toggleBtnBottomByStepCondition(
        stepCondition: Boolean,
        context: Context,
    ) {
        binding.btnBottom.isClickable = stepCondition

        if (stepCondition) {
            changeBtnBottomAsAvailableView(context)
        } else {
            changeBtnBottomAsUnavailableView(context)
        }
    }

    private fun changeBtnBottomAsAvailableView(context: Context) {
        binding.flNext.backgroundTintList =
            ColorStateList.valueOf(context.getColor(R.color.purple_5))
        binding.btnBottom.setTextColor(context.getColor(R.color.white))
    }

    private fun changeBtnBottomAsUnavailableView(context: Context) {
        binding.flNext.backgroundTintList =
            ColorStateList.valueOf(context.getColor(R.color.grey_3))
        binding.btnBottom.setTextColor(context.getColor(R.color.grey_5))
    }

    private fun switchBtnBottomTextByStep(
        step: SignUpStep,
        context: Context,
    ) {
        val btnBottom =
            when (step) {
                SignUpStep.TERMS -> context.getString(R.string.btn_sign_up)
                else -> context.getString(R.string.btn_next)
            }

        binding.btnBottom.text = btnBottom
    }

    private fun setBtnBottomListenerByCurrentStep(step: SignUpStep) {
        when (step) {
            SignUpStep.TERMS -> setBtnBottomAsSignUp()
            else -> setBtnBottomAsNext()
        }
    }

    private fun setBtnBottomAsSignUp() {
        binding.btnBottom.setOnClickListener {
            viewModel.signUp()
        }
    }

    private fun setBtnBottomAsNext() {
        binding.btnBottom.setOnClickListener {
            viewModel.next()
        }
    }

    private fun initListener() {
        setBtnBackListener()
        setBtnCancelListener()
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            if (binding.vpgSignUp.currentItem == 0) {
                finish()
                return@setOnClickListener
            }

            viewModel.previous()
        }
    }

    private fun setBtnCancelListener() {
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun switchView(view: View) {
        binding.pgbProcessing.isVisible = view == binding.pgbProcessing
        binding.btnBottom.isVisible = view == binding.btnBottom
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?,
    ): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (binding.vpgSignUp.currentItem == 0) {
                super.onKeyDown(keyCode, event)
            } else {
                viewModel.previous()
                true
            }
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}
