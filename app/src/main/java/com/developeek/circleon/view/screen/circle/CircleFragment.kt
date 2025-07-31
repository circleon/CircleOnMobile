package com.developeek.circleon.view.screen.circle

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
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
import com.developeek.circleon.view.screen.base.BaseFragment
import com.developeek.circleon.view.viewmodel.circle.CircleViewModel
import com.developeek.circleon.view.viewmodelimpl.circle.CircleScreen
import com.developeek.circleon.view.viewmodelimpl.circle.CircleViewModelImpl
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CircleFragment : BaseFragment() {
    private val binding: FragmentCircleBinding by lazy {
        FragmentCircleBinding.inflate(layoutInflater)
    }
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
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initListener()
        handleEventFlow(requireContext())
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

    private fun handleEventFlow(context: Context) {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        super.handleEvent(it)
                    }
                }
                launch {
                    viewModel.screenFlow.collect {
                        handleScreenFlow(it, requireContext())
                    }
                }
            }
        }
    }

    private fun handleScreenFlow(
        screenFlow: CircleScreen,
        context: Context,
    ) {
        binding.pgbLoading.isVisible = screenFlow is CircleScreen.Loading
        when (screenFlow) {
            is CircleScreen.Success -> showSuccessView(screenFlow, context)
            else -> {}
        }
    }

    private fun showSuccessView(
        screenFlow: CircleScreen.Success,
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
