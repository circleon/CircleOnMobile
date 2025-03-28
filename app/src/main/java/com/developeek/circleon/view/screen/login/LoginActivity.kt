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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.developeek.circleon.databinding.ActivityLoginBinding
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.screen.HomeActivity
import com.developeek.circleon.view.viewmodel.login.LoginViewModel
import com.developeek.circleon.view.viewmodelimpl.login.LoginViewModelImpl
import com.developeek.circleon.view.widget.ErrorAlertDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels<LoginViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initObserver(this)
        initListener(this)
    }

    private fun initObserver(activity: Activity) {
        viewModel.state.observe(
            activity as LifecycleOwner,
            stateObserver(activity),
        )
    }

    private fun stateObserver(activity: Activity) =
        Observer<UiState> {
            binding.pgbLoading.isVisible = it is UiState.Loading
            binding.btnLogin.isVisible = it !is UiState.Loading
            when (it) {
                UiState.Success -> sendUserToHomeScreen(activity)
                UiState.ServiceError ->
                    ErrorAlertDialog(
                        activity,
                        viewModel.error,
                    ).show()
                else -> {}
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
}
