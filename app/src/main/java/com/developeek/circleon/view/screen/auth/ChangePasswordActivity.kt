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
import com.developeek.circleon.databinding.ActivityChangePasswordBinding
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.adapter.ChangePasswordFragmentAdapter
import com.developeek.circleon.view.viewmodel.auth.ChangePasswordViewModel
import com.developeek.circleon.view.viewmodelimpl.auth.ChangePasswordScreen
import com.developeek.circleon.view.viewmodelimpl.auth.ChangePasswordScreenEvent
import com.developeek.circleon.view.viewmodelimpl.auth.ChangePasswordStep
import com.developeek.circleon.view.viewmodelimpl.auth.ChangePasswordViewModelImpl
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity() {
    private val binding: ActivityChangePasswordBinding by lazy {
        ActivityChangePasswordBinding.inflate(layoutInflater)
    }
    private val viewModel: ChangePasswordViewModel by viewModels<ChangePasswordViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViewPager(supportFragmentManager, lifecycle)
        initListener()
        handleEventFlow(this)
    }

    private fun initViewPager(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) {
        binding.vpgChangePassword.adapter = ChangePasswordFragmentAdapter(fragmentManager, lifecycle)
        binding.vpgChangePassword.isUserInputEnabled = false
        binding.vpgChangePassword.offscreenPageLimit = 1
    }

    private fun initListener() {
        setBtnBackListener()
        setBtnCancelListener()
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            if (binding.vpgChangePassword.currentItem == 0) {
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

    private fun handleEventFlow(context: Context) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        handleEvent(it, this@ChangePasswordActivity)
                    }
                }
                launch {
                    viewModel.screenFlow.collect {
                        handleScreenFlow(it)
                    }
                }
                launch {
                    viewModel.changePasswordScreenEvent.collect {
                        handleChangePasswordScreenEvent(it, context)
                    }
                }
            }
        }
    }

    private fun handleEvent(
        event: Event,
        context: Context,
    ) {
        when (event) {
            is Event.ShowToast -> showToast(event, context)
            is Event.ShowDialog -> showDialog(event, context)
            is Event.ShowProcessing -> switchView(binding.pgbProcessing)
            is Event.EndProcessing -> switchView(binding.btnBottom)
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

    private fun handleScreenFlow(screenFlow: ChangePasswordScreen) {
        if (screenFlow is ChangePasswordScreen.Loading) {
            switchView(binding.pgbProcessing)
        } else {
            switchView(binding.btnBottom)
        }
        when (screenFlow) {
            is ChangePasswordScreen.Success -> finish()
            else -> {}
        }
    }

    private fun handleChangePasswordScreenEvent(
        event: ChangePasswordScreenEvent,
        context: Context,
    ) {
        when (event) {
            is ChangePasswordScreenEvent.UpdateChangePasswordProcess -> updateChangePasswordProcess(event, context)
        }
    }

    private fun updateChangePasswordProcess(
        event: ChangePasswordScreenEvent.UpdateChangePasswordProcess,
        context: Context,
    ) {
        event.let {
            binding.vpgChangePassword.currentItem = it.step.getIndex()
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
        step: ChangePasswordStep,
        context: Context,
    ) {
        val btnBottom =
            when (step) {
                ChangePasswordStep.PASSWORD -> context.getString(R.string.btn_change_password)
                else -> context.getString(R.string.btn_next)
            }

        binding.btnBottom.text = btnBottom
    }

    private fun setBtnBottomListenerByCurrentStep(step: ChangePasswordStep) {
        when (step) {
            ChangePasswordStep.PASSWORD -> setBtnBottomAsChangePassword()
            else -> setBtnBottomAsNext()
        }
    }

    private fun setBtnBottomAsChangePassword() {
        binding.btnBottom.setOnClickListener {
            viewModel.changePassword()
        }
    }

    private fun setBtnBottomAsNext() {
        binding.btnBottom.setOnClickListener {
            viewModel.next()
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
            if (binding.vpgChangePassword.currentItem == 0) {
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
