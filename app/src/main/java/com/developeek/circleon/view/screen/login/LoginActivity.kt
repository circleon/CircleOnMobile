package com.developeek.circleon.view.screen.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.developeek.circleon.databinding.ActivityLoginBinding
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.screen.HomeActivity
import com.developeek.circleon.view.viewmodel.login.LoginViewModel
import com.developeek.circleon.view.viewmodelimpl.login.LoginScreenEvent
import com.developeek.circleon.view.viewmodelimpl.login.LoginViewModelImpl
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels<LoginViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        handleEvent(it, this@LoginActivity)
                    }
                }
                launch {
                    viewModel.loginScreenEvent.collect {
                        handleLoginScreenEvent(it, this@LoginActivity)
                    }
                }
            }
        }
    }

    private fun handleEvent(
        event: Event,
        activity: Activity,
    ) {
        if (event is Event.ShowProcessing) {
            switchView(binding.pgbLoading)
        } else {
            switchView(binding.btnLogin)
        }

        when (event) {
            is Event.ShowToast -> showToast(event, activity)
            is Event.ShowDialog -> showDialog(event, activity)
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
        SingleMessageAlertDialog(
            context,
            event.message,
        ).show()
    }

    private fun handleLoginScreenEvent(
        event: LoginScreenEvent,
        activity: Activity,
    ) {
        when (event) {
            is LoginScreenEvent.SendToHomeScreen -> sendUserToHomeScreen(activity)
        }
    }

    private fun sendUserToHomeScreen(activity: Activity) {
        val intent = Intent(activity, HomeActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(intent)
    }

    private fun initListener(activity: Activity) {
        setBtnLoginListener()
        setBtnSignUpListener(activity)
        setEdtPasswordListener(activity)
    }

    private fun setBtnLoginListener() {
        binding.btnLogin.setOnClickListener {
            login()
        }
    }

    private fun setBtnSignUpListener(activity: Activity) {
        binding.btnSignUp.setOnClickListener {
            sendUserToSignUpScreen(activity)
        }
    }

    private fun setEdtPasswordListener(context: Context) {
        binding.edtPassword.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) {
                login()
                context.getSystemService(InputMethodManager::class.java).also {
                    it.hideSoftInputFromWindow(binding.edtPassword.windowToken, 0)
                }
            }
            true
        }
    }

    private fun login() {
        viewModel.login(
            binding.edtEmail.text.toString(),
            binding.edtPassword.text.toString(),
        )
    }

    private fun sendUserToSignUpScreen(activity: Activity) {
        val intent = Intent(activity, SignUpActivity::class.java)

        startActivity(intent)
    }

    private fun switchView(view: View) {
        binding.pgbLoading.isVisible = view == binding.pgbLoading
        binding.btnLogin.isVisible = view == binding.btnLogin
    }
}
