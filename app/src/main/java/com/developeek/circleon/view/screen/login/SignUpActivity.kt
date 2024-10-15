package com.developeek.circleon.view.screen.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.developeek.circleon.databinding.ActivitySignUpBinding
import com.developeek.circleon.view.adapter.SignUpFragmentAdapter

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewPager(supportFragmentManager, lifecycle)
        initListener()
    }

    private fun initViewPager(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) {
        binding.vpgSignUp.adapter = SignUpFragmentAdapter(fragmentManager, lifecycle)
    }

    private fun initListener() {
        binding.btnFinish.setOnClickListener {
            finish()
        }
    }
}
