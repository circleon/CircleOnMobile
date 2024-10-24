package com.developeek.circleon.view.screen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.developeek.circleon.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: access 토큰 갱신 결과에 따라 로그인 화면 분기하도록 수정
    }
}
