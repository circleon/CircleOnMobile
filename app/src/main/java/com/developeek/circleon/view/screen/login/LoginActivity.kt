package com.developeek.circleon.view.screen.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.developeek.circleon.databinding.ActivityLoginBinding
import com.developeek.circleon.view.screen.HomeActivity
import com.developeek.circleon.view.viewmodel.login.LoginViewModel
import com.developeek.circleon.view.viewmodelimpl.login.Event
import com.developeek.circleon.view.viewmodelimpl.login.LoginViewModelImpl
import com.developeek.circleon.view.widget.ErrorAlertDialog
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
                viewModel.event.collect {
                    handleEvent(it, this@LoginActivity)
                }
            }
        }
    }

    private fun handleEvent(
        event: Event,
        activity: Activity,
    ) {
        binding.pgbLoading.isVisible = event is Event.ShowLoadingView
        binding.btnLogin.isVisible = event !is Event.ShowLoadingView
        when (event) {
            is Event.SendToHomeScreen -> sendUserToHomeScreen(activity)
            is Event.ShowErrorDialog -> showErrorDialog(activity, event.text)
            else -> {}
        }
    }

    private fun sendUserToHomeScreen(activity: Activity) {
        val intent = Intent(activity, HomeActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(intent)
    }

    private fun showErrorDialog(
        context: Context,
        text: String,
    ) {
        ErrorAlertDialog(
            context,
            text,
        ).show()
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
}
