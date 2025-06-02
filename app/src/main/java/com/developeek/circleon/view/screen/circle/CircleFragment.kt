package com.developeek.circleon.view.screen.circle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentCircleBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.circle.CircleViewModel
import com.developeek.circleon.view.viewmodelimpl.circle.CircleScreen
import com.developeek.circleon.view.viewmodelimpl.circle.CircleViewModelImpl
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

        initListener()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        handleEvent(it, requireActivity(), requireContext())
                    }
                }
                launch {
                    viewModel.screenFlow.collect {
                        handleScreenFlow(it, requireContext())
                    }
                }
            }
        }
        viewModel.refresh() // 탭 전환 시 자동 새로고침
    }

    private fun initListener() {
        setBtnUploadCircleListener()
    }

    private fun setBtnUploadCircleListener() {
        binding.clUploadCircle.setOnClickListener {
            sendUserToUploadCircleScreen()
        }
    }

    private fun sendUserToUploadCircleScreen() {
        findNavController().navigate(
            R.id.action_circleFragment_to_uploadCircleFragment2,
            bundleOf(
                Pair(Const.FLAG_EDIT_SCREEN, false),
            ),
        )
    }

    private fun handleEvent(
        event: Event,
        parentActivity: Activity,
        context: Context,
    ) {
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

    private fun handleScreenFlow(
        screenFlow: CircleScreen,
        context: Context,
    ) {
        binding.pgbLoading.isVisible = screenFlow is CircleScreen.LoadingView
        when (screenFlow) {
            is CircleScreen.SuccessView -> showSuccessView(screenFlow, context)
            else -> {}
        }
    }

    private fun showSuccessView(
        screenFlow: CircleScreen.SuccessView,
        context: Context,
    ) {
        screenFlow.let {
            loadUserCircles(
                myCircles = it.myCircles,
                joinRequestedCircles = it.joinRequestedCircles,
                leaveRequestedCircles = it.leaveRequestedCircles,
                context,
            )
            setUserCircleCardListener(
                myCircles = it.myCircles,
                joinRequestedCircles = it.joinRequestedCircles,
                leaveRequestedCircles = it.leaveRequestedCircles,
            )
        }
    }

    private fun loadUserCircles(
        myCircles: Models<CircleSummaryModel>,
        joinRequestedCircles: Models<CircleSummaryModel>,
        leaveRequestedCircles: Models<CircleSummaryModel>,
        context: Context,
    ) {
        loadMyCircles(context, myCircles)
        loadMyJoinRequestedCircles(context, joinRequestedCircles)
        loadMyLeaveRequestedCircles(context, leaveRequestedCircles)
    }

    private fun loadMyCircles(
        context: Context,
        circles: Models<CircleSummaryModel>,
    ) {
        binding.txtMyCircleCount.text =
            String.format(
                context.getString(R.string.circle_content_my_circle),
                circles.size(),
            )
    }

    private fun loadMyJoinRequestedCircles(
        context: Context,
        circles: Models<CircleSummaryModel>,
    ) {
        binding.txtJoinRequestedCircleCount.text =
            String.format(
                context.getString(R.string.circle_content_requested_circle),
                circles.size(),
            )
    }

    private fun loadMyLeaveRequestedCircles(
        context: Context,
        circles: Models<CircleSummaryModel>,
    ) {
        binding.txtLeaveRequestedCircleCount.text =
            String.format(
                context.getString(R.string.circle_content_requested_circle),
                circles.size(),
            )
    }

    private fun setUserCircleCardListener(
        myCircles: Models<CircleSummaryModel>,
        joinRequestedCircles: Models<CircleSummaryModel>,
        leaveRequestedCircles: Models<CircleSummaryModel>,
    ) {
        binding.clMyCircle.setOnClickListener {
            sendUserToCircleListScreen(myCircles, MembershipStatus.JOINED)
        }
        binding.clJoinRequestedCircle.setOnClickListener {
            sendUserToCircleListScreen(joinRequestedCircles, MembershipStatus.JOIN_REQUESTED)
        }
        binding.clLeaveRequestedCircle.setOnClickListener {
            sendUserToCircleListScreen(leaveRequestedCircles, MembershipStatus.LEAVE_REQUESTED)
        }
    }

    private fun sendUserToCircleListScreen(
        circles: Models<CircleSummaryModel>,
        membershipStatus: MembershipStatus,
    ) {
        val bundle = Bundle()

        bundle.putSerializable(Const.TAG_CIRCLE_SUMMARIES, circles)
        bundle.putSerializable(Const.TAG_MEMBERSHIP_STATUS, membershipStatus)
        findNavController().navigate(R.id.action_circleFragment_to_myCircleFragment, bundle)
    }
}
