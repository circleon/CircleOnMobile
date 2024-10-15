package com.developeek.circleon.view.screen.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.developeek.circleon.databinding.ActivityLoginBinding
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.view.screen.home.HomeActivity
import com.developeek.circleon.view.viewmodelimpl.LoginViewModelImpl
import com.developeek.circleon.view.widget.CustomAlertDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModelImpl by viewModels()

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
            when (it) {
                UiState.Success -> sendUserToHomeScreen(activity)
                UiState.Timeout, UiState.Error ->
                    CustomAlertDialog(
                        activity,
                        viewModel.error,
                    ).show()
                else -> {}
            }
        }

    private fun sendUserToHomeScreen(activity: Activity) {
        val intent = Intent(activity, HomeActivity::class.java)

        startActivity(intent)
    }

    private fun initListener(activity: Activity) {
        setBtnLoginListener()
        setBtnSignUpListener(activity)
    }

    private fun setBtnLoginListener() {
        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.edtEmail.text.toString(),
                binding.edtPassword.text.toString(),
            )
        }
    }

    private fun setBtnSignUpListener(activity: Activity) {
        binding.btnSignUp.setOnClickListener {
            sendUserToSignUpScreen(activity)
        }
    }

    private fun sendUserToSignUpScreen(activity: Activity) {
        val intent = Intent(activity, SignUpActivity::class.java)

        startActivity(intent)
    }
}
