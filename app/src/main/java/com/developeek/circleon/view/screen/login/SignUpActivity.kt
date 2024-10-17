package com.developeek.circleon.view.screen.login

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
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

        setSupportActionBar(binding.tbSignUp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

    private fun initListener() {
        setBtnFinishListener()
        setBtnNextListener()
    }

    private fun setBtnFinishListener() {
        binding.btnFinish.setOnClickListener {
            finish()
        }
    }

    private fun setBtnNextListener() {
        binding.btnNext.setOnClickListener {
            binding.vpgSignUp.currentItem += 1
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                binding.vpgSignUp.currentItem -= 1
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
