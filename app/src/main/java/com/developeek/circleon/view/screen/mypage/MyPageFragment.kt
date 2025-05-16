package com.developeek.circleon.view.screen.mypage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.TokenManager
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentMyPageBinding
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.screen.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyPageFragment : Fragment() {
    private lateinit var binding: FragmentMyPageBinding

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findViewById<BottomNavigationView>(R.id.btmNav).selectedItemId = R.id.nav_graph_home
        }
    }

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

        // TODO: userManager 유저 삭제, 로그아웃 api 연동
        binding.btnLogout.setOnClickListener {
            tokenManager.deleteAccessToken()
            tokenManager.deleteRefreshToken()
            userManager.deleteUser()
            sendUserToLoginScreen(requireActivity())
        }

        initView()
        initListener()
    }

    private fun initView() {
        initUserName()
    }

    private fun initUserName() {
        userManager.getUser()?.let {
            binding.txtUserName.text = it.name
        }
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun initListener() {
        setBtnMyPostsListener()
        setBtnSendFeedbackListener()
    }

    private fun setBtnMyPostsListener() {
        binding.btnMyPosts.setOnClickListener {
            sendUserToMyPostScreen(true)
        }
        binding.btnMyCommentPosts.setOnClickListener {
            sendUserToMyPostScreen(false)
        }
    }

    private fun sendUserToMyPostScreen(isMyPosts: Boolean) {
        val bundle = Bundle()

        bundle.putBoolean(Const.TAGE_MY_POSTS, isMyPosts)
        findNavController().navigate(R.id.action_myPageFragment_to_myPostFragment, bundle)
    }

    private fun setBtnSendFeedbackListener() {
        binding.btnSendFeedback.setOnClickListener {
            // sendFeedback
        }
    }
}
