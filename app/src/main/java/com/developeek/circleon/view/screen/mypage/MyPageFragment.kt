package com.developeek.circleon.view.screen.mypage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.BuildConfig
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentMyPageBinding
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.mypage.MyPageViewModel
import com.developeek.circleon.view.viewmodelimpl.mypage.MyPageViewModelImpl
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyPageFragment : Fragment() {
    private lateinit var binding: FragmentMyPageBinding
    private val viewModel: MyPageViewModel by viewModels<MyPageViewModelImpl>()

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

        initView(requireContext())
        initListener()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect {
                    handleEvent(it, requireActivity(), requireContext())
                }
            }
        }
    }

    private fun initView(context: Context) {
        initUserName()
        initVersionName(context)
    }

    private fun initUserName() {
        userManager.getUser()?.let {
            binding.txtUserName.text = it.name
        }
    }

    private fun initVersionName(context: Context) {
        binding.txtVersionName.text =
            String.format(context.getString(R.string.version_name), BuildConfig.VERSION_NAME)
    }

    private fun initListener() {
        setBtnMyPostsListener()
        setBtnSendFeedbackListener()
        setBtnLogoutListener()
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

    private fun setBtnLogoutListener() {
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun handleEvent(
        event: Event,
        parentActivity: Activity,
        context: Context,
    ) {
        binding.pgbLoading.isVisible = event is Event.ShowProcessing
        when (event) {
            is Event.SendToLoginScreen -> sendUserToLoginScreen(parentActivity)
            is Event.ShowToast -> showToast(event, context)
            else -> {}
        }
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showToast(
        event: Event.ShowToast,
        context: Context,
    ) {
        if (SingleMessageToast.previousFinished()) {
            SingleMessageToast(context, event.message).show()
        }
    }
}
