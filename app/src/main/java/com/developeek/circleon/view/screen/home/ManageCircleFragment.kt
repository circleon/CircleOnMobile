package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentManageCircleBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.home.ManageCircleViewModel
import com.developeek.circleon.view.viewmodelimpl.home.ManageCircleScreen
import com.developeek.circleon.view.viewmodelimpl.home.ManageCircleViewModelImpl
import com.developeek.circleon.view.widget.PositiveAlertDialog
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageCircleFragment : Fragment() {
    private lateinit var binding: FragmentManageCircleBinding
    private lateinit var circle: CircleDetailModel
    private val viewModel: ManageCircleViewModel by viewModels<ManageCircleViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<ManageCircleViewModelImpl.ManageCircleViewModelFactory> {
                    it.create(circle)
                }
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circle = it.getSerializable(Const.TAG_CIRCLE_DETAIL) as CircleDetailModel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentManageCircleBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireActivity())
        initListener(requireContext())
        initRefreshObserver()
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
    }

    private fun initView(parentActivity: Activity) {
        hideBtmNav(parentActivity)
        hideBtnRequestOfficialStatusWhenAlreadyOfficial()
    }

    private fun hideBtmNav(activity: Activity) {
        activity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    private fun hideBtnRequestOfficialStatusWhenAlreadyOfficial() {
        binding.btnRequestOfficialStatus.isVisible = !circle.isOfficial()
    }

    private fun initListener(context: Context) {
        setBtnCancelListener()
        setBtnRequestOfficialStatus(context)
    }

    private fun setBtnCancelListener() {
        binding.btnCancel.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().popBackStack()
    }

    private fun setBtnRequestOfficialStatus(context: Context) {
        binding.btnRequestOfficialStatus.setOnClickListener {
            showRequestOfficialStatusDialog(context)
        }
    }

    private fun showRequestOfficialStatusDialog(context: Context) {
        PositiveAlertDialog(
            context,
            message = context.getString(R.string.message_request_official_status),
            positiveButton = context.getString(R.string.btn_request),
            positiveListener = {
                viewModel.requestOfficialStatus()
            },
        ).show()
    }

    private fun initRefreshObserver() {
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.let {
                it.getLiveData<Boolean>(Const.FLAG_CIRCLE_DATA_CHANGED)
                    .observe(viewLifecycleOwner) { dataChanged ->
                        if (dataChanged) {
                            viewModel.refresh()
                            requestRefreshToPreviousScreen()
                            it[Const.FLAG_CIRCLE_DATA_CHANGED] = false
                        }
                    }
            }
    }

    private fun requestRefreshToPreviousScreen() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_CIRCLE_DATA_CHANGED, true)
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
            is Event.ShowDialog -> showDialog(event, context)
            else -> {}
        }
    }

    private fun sendUserToLoginScreen(parentActivity: Activity) {
        val intent = Intent(parentActivity, LoginActivity::class.java)
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

    private fun showDialog(
        event: Event.ShowDialog,
        context: Context,
    ) {
        SingleMessageAlertDialog(context, event.message).show()
    }

    private fun handleScreenFlow(
        screenFlow: ManageCircleScreen,
        context: Context,
    ) {
        binding.pgbLoading.isVisible = screenFlow is ManageCircleScreen.LoadingView
        when (screenFlow) {
            is ManageCircleScreen.SuccessView -> showSuccessView(screenFlow, context)
            else -> {}
        }
    }

    private fun showSuccessView(
        screenFlow: ManageCircleScreen.SuccessView,
        context: Context,
    ) {
        screenFlow.let {
            loadMembers(
                circleMembers = it.circleMembers,
                joinRequestedMembers = it.joinRequestedMembers,
                leaveRequestedMembers = it.leaveRequestedMembers,
                context,
            )
            setMemberCardListener(
                circleMembers = it.circleMembers,
                joinRequestedMembers = it.joinRequestedMembers,
                leaveRequestedMembers = it.leaveRequestedMembers,
            )
        }
    }

    private fun loadMembers(
        circleMembers: Models<MemberModel>,
        joinRequestedMembers: Models<MemberModel>,
        leaveRequestedMembers: Models<MemberModel>,
        context: Context,
    ) {
        loadCircleMembers(circleMembers, context)
        loadJoinRequestedMembers(joinRequestedMembers, context)
        loadLeaveRequestedMembers(leaveRequestedMembers, context)
    }

    private fun loadCircleMembers(
        members: Models<MemberModel>,
        context: Context,
    ) {
        binding.txtCircleMember.text =
            String.format(
                context.getString(R.string.manage_circle_content_circle_member),
                members.size(),
            )
    }

    private fun loadJoinRequestedMembers(
        members: Models<MemberModel>,
        context: Context,
    ) {
        binding.txtJoinRequestedMember.text =
            String.format(
                context.getString(R.string.manage_circle_content_join_requested_member),
                members.size(),
            )
    }

    private fun loadLeaveRequestedMembers(
        members: Models<MemberModel>,
        context: Context,
    ) {
        binding.txtLeaveRequestedMember.text =
            String.format(
                context.getString(R.string.manage_circle_content_leave_requested_member),
                members.size(),
            )
    }

    private fun setMemberCardListener(
        circleMembers: Models<MemberModel>,
        joinRequestedMembers: Models<MemberModel>,
        leaveRequestedMembers: Models<MemberModel>,
    ) {
        binding.clCircleMember.setOnClickListener {
            sendUserToMemberListScreen(circleMembers, MembershipStatus.JOINED)
        }
        binding.clApproveCircleJoin.setOnClickListener {
            sendUserToMemberListScreen(joinRequestedMembers, MembershipStatus.JOIN_REQUESTED)
        }
        binding.clApproveCircleLeave.setOnClickListener {
            sendUserToMemberListScreen(leaveRequestedMembers, MembershipStatus.LEAVE_REQUESTED)
        }
    }

    private fun sendUserToMemberListScreen(
        members: Models<MemberModel>,
        membershipStatus: MembershipStatus,
    ) {
        val bundle = Bundle()

        bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, circle)
        bundle.putSerializable(Const.TAG_MEMBERS, members)
        bundle.putSerializable(Const.TAG_MEMBERSHIP_STATUS, membershipStatus)
        findNavController().navigate(R.id.action_manageCircleFragment_to_manageCircleMemberFragment, bundle)
    }
}
