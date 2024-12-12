package com.developeek.circleon.view.screen.mypage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.developeek.circleon.data.source.manager.TokenManager
import com.developeek.circleon.databinding.FragmentMyPageBinding
import com.developeek.circleon.view.screen.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyPageFragment : Fragment() {
    private lateinit var binding: FragmentMyPageBinding

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyPageBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogOut.setOnClickListener {
            tokenManager.deleteAccessToken()
            tokenManager.deleteRefreshToken()
            sendUserToLoginScreen(requireActivity())
        }
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}
