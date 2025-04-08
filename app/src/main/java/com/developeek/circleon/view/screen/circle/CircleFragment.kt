package com.developeek.circleon.view.screen.circle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentCircleBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.circle.CircleViewModel
import com.developeek.circleon.view.viewmodelimpl.circle.CircleViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CircleFragment : Fragment() {
    private lateinit var binding: FragmentCircleBinding
    private val viewModel: CircleViewModel by viewModels<CircleViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            selectHomeTab(requireActivity())
        }
    }

    private fun selectHomeTab(parentActivity: Activity) {
        parentActivity.findViewById<BottomNavigationView>(R.id.btmNav).selectedItemId = R.id.nav_graph_home
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initObserver(requireActivity(), requireContext())
        viewModel.refresh() // 탭 전환 시 자동 새로고침
    }

    private fun initObserver(
        parentActivity: Activity,
        context: Context,
    ) {
        viewModel.state.observe(
            viewLifecycleOwner,
            stateObserver(parentActivity, context),
        )
    }

    private fun stateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        binding.pgbLoading.isVisible = it is UiState.Loading
        when (it) {
            UiState.Success -> {
                loadUserCircles(context)
                setUserCircleCardListener()
            }
            UiState.AuthenticationError -> {
                sendUserToLoginScreen(parentActivity)
                showErrorToast(context)
            }
            UiState.ServiceError -> {
                showErrorToast(context)
            }
            else -> {}
        }
    }

    private fun loadUserCircles(context: Context) {
        loadMyCircles(context, viewModel.myCircles)
        loadMyJoinRequestedCircles(context, viewModel.myJoinRequestedCircles)
    }

    private fun loadMyCircles(
        context: Context,
        circles: CircleSummaryModels,
    ) {
        binding.txtMyCircleCount.text =
            String.format(
                ContextCompat.getString(context, R.string.circle_content_my_circle),
                circles.size(),
            )
    }

    private fun loadMyJoinRequestedCircles(
        context: Context,
        circles: CircleSummaryModels,
    ) {
        binding.txtJoinRequestedCircleCount.text =
            String.format(
                ContextCompat.getString(context, R.string.circle_content_requested_circle),
                circles.size(),
            )
    }

    private fun setUserCircleCardListener() {
        binding.clMyCircle.setOnClickListener {
            sendUserToCircleListScreen(viewModel.myCircles, MembershipStatus.JOINED)
        }
        binding.clJoinRequestedCircle.setOnClickListener {
            sendUserToCircleListScreen(viewModel.myJoinRequestedCircles, MembershipStatus.JOIN_REQUESTED)
        }
    }

    private fun sendUserToCircleListScreen(
        circles: CircleSummaryModels,
        membershipStatus: MembershipStatus,
    ) {
        val bundle = Bundle()

        bundle.putSerializable(Const.TAG_CIRCLE_SUMMARIES, circles)
        bundle.putSerializable(Const.TAG_MEMBERSHIP_STATUS, membershipStatus)
        findNavController().navigate(R.id.action_circleFragment_to_myCircleFragment, bundle)
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showErrorToast(context: Context) {
        if (ErrorToast.previousFinished()) {
            ErrorToast(context, viewModel.error).show()
        }
    }
}
