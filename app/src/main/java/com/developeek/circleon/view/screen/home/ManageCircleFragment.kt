package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentManageCircleBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.ManageCircleViewModel
import com.developeek.circleon.view.viewmodelimpl.ManageCircleViewModelImpl
import com.developeek.circleon.view.widget.ErrorAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback

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

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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
        initObserver(requireActivity(), requireContext())
        initListener()
    }

    private fun initView(parentActivity: Activity) {
        hideBtmNav(parentActivity)
    }

    private fun hideBtmNav(activity: Activity) {
        activity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    private fun initObserver(
        parentActivity: Activity,
        context: Context,
    ) {
        viewModel.state.observe(
            viewLifecycleOwner,
            stateObserver(parentActivity, context),
        )
        // 정보 수정 여부 감지
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

    private fun stateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        binding.pgbLoading.isVisible = it is UiState.Loading
        when (it) {
            UiState.Success -> {
                loadMembers(context)
                setMemberCardListener()
            }
            UiState.AuthenticationError -> {
                sendUserToLoginScreen(parentActivity)
                showErrorToast(context)
            }
            UiState.ServiceError -> {
                showErrorDialog(context)
            }
            else -> {}
        }
    }

    private fun loadMembers(context: Context) {
        loadCircleMembers(context, viewModel.circleMembers)
        loadCircleJoinRequestedMembers(context, viewModel.joinRequestedMembers)
        loadCircleLeaveRequestedMembers(context, viewModel.leaveRequestedMembers)
    }

    private fun loadCircleMembers(
        context: Context,
        members: MemberModels,
    ) {
        binding.txtCircleMember.text =
            String.format(
                ContextCompat.getString(context, R.string.manage_circle_content_circle_member),
                members.size(),
            )
    }

    private fun loadCircleJoinRequestedMembers(
        context: Context,
        members: MemberModels,
    ) {
        binding.txtJoinRequestedMember.text =
            String.format(
                ContextCompat.getString(context, R.string.manage_circle_content_join_requested_member),
                members.size(),
            )
    }

    private fun loadCircleLeaveRequestedMembers(
        context: Context,
        members: MemberModels,
    ) {
        binding.txtLeaveRequestedMember.text =
            String.format(
                ContextCompat.getString(context, R.string.manage_circle_content_leave_requested_member),
                members.size(),
            )
    }

    private fun initListener() {
        setBtnCancelListener()
    }

    private fun setBtnCancelListener() {
        binding.btnCancel.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun setMemberCardListener() {
        binding.clCircleMember.setOnClickListener {
            sendUserToMemberListScreen(viewModel.circleMembers, MembershipStatus.JOINED)
        }
        binding.clApproveCircleJoin.setOnClickListener {
            sendUserToMemberListScreen(viewModel.joinRequestedMembers, MembershipStatus.JOIN_REQUESTED)
        }
        binding.clApproveCircleLeave.setOnClickListener {
            sendUserToMemberListScreen(viewModel.leaveRequestedMembers, MembershipStatus.LEAVE_REQUESTED)
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().navigateUp()
    }

    private fun sendUserToLoginScreen(parentActivity: Activity) {
        val intent = Intent(parentActivity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showErrorToast(context: Context) {
        if (ErrorToast.previousFinished()) {
            ErrorToast(context, viewModel.error).show()
        }
    }

    private fun showErrorDialog(context: Context) {
        ErrorAlertDialog(context, viewModel.error).show()
    }

    private fun sendUserToMemberListScreen(
        members: MemberModels,
        membershipStatus: MembershipStatus,
    ) {
        val bundle = Bundle()

        bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, circle)
        bundle.putSerializable(Const.TAG_MEMBERS, members)
        bundle.putSerializable(Const.TAG_MEMBERSHIP_STATUS, membershipStatus)
        findNavController().navigate(R.id.action_manageCircleFragment_to_manageCircleMemberFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }
}
