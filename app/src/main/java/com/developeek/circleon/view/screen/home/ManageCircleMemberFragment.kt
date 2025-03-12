package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentManageCircleMemberBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CircleMemberAdapter
import com.developeek.circleon.view.adapter.JoinRequestedMemberAdapter
import com.developeek.circleon.view.adapter.LeaveRequestedMemberAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.ManageCircleMemberViewModel
import com.developeek.circleon.view.viewmodelimpl.ManageCircleMemberViewModelImpl
import com.developeek.circleon.view.widget.CircleMemberLeaveRequestAcceptAlertDialog
import com.developeek.circleon.view.widget.CircleMemberRoleEditAlertDialog
import com.developeek.circleon.view.widget.ContentDeleteAlertDialog
import com.developeek.circleon.view.widget.ErrorAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import javax.inject.Inject

@AndroidEntryPoint
class ManageCircleMemberFragment : Fragment() {
    private lateinit var binding: FragmentManageCircleMemberBinding
    private lateinit var circle: CircleDetailModel
    private lateinit var members: MemberModels
    private lateinit var membershipStatus: MembershipStatus
    private val viewModel: ManageCircleMemberViewModel by viewModels<ManageCircleMemberViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<ManageCircleMemberViewModelImpl.ManageCircleMemberViewModelFactory> {
                    it.create(circle, members, membershipStatus)
                }
        },
    )

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circle = it.getSerializable(Const.TAG_CIRCLE_DETAIL) as CircleDetailModel
            members = it.getSerializable(Const.TAG_MEMBERS) as MemberModels
            membershipStatus = it.getSerializable(Const.TAG_MEMBERSHIP_STATUS) as MembershipStatus
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentManageCircleMemberBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireActivity(), requireContext())
        initObserver(requireActivity(), requireContext())
        load(requireContext(), viewModel.members)
    }

    private fun initView(
        parentActivity: Activity,
        context: Context,
    ) {
        initToolbar(context)
        initMemberRecyclerViewByMembershipStatus(context)
        hideBtmNav(parentActivity)
    }

    private fun initToolbar(context: Context) {
        val title =
            when (membershipStatus) {
                MembershipStatus.JOINED ->
                    ContextCompat.getString(context, R.string.manage_circle_title_circle_member)
                MembershipStatus.JOIN_REQUESTED ->
                    ContextCompat.getString(context, R.string.manage_circle_title_approve_circle_join)
                MembershipStatus.LEAVE_REQUESTED ->
                    ContextCompat.getString(context, R.string.manage_circle_title_approve_circle_leave)
                else -> Const.EMPTY_TEXT
            }

        binding.txtTbTitle.text = title
    }

    private fun initMemberRecyclerViewByMembershipStatus(context: Context) {
        when (membershipStatus) {
            MembershipStatus.JOINED -> {
                initCircleMemberView(context)
            }
            MembershipStatus.JOIN_REQUESTED -> {
                initJoinRequestedMemberView(context)
            }
            MembershipStatus.LEAVE_REQUESTED -> {
                initLeaveRequestedMemberView(context)
            }
            else -> {}
        }
    }

    private fun initCircleMemberView(context: Context) {
        binding.rvMember.adapter =
            CircleMemberAdapter(
                context,
                glideProvider,
                circle.role,
                circle.memberId,
                overflowListenerInitializer =
                    object : ItemListenerInitializer<MemberModel> {
                        override fun initialize(item: MemberModel) {
                        }

                        override fun initialize(
                            item: MemberModel,
                            view: View?,
                        ) {
                            initProfileOverflowMenuAndShow(context, item, view!!)
                        }
                    },
            )
        binding.rvMember.layoutManager = LinearLayoutManager(context)
    }

    private fun initProfileOverflowMenuAndShow(
        context: Context,
        item: MemberModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(context, view) {}
        if (circle.isUserPresident()) {
            popupMenu.inflate(R.menu.menu_president_circle_member_settings)
        } else if (circle.isUserExecutive()) {
            popupMenu.inflate(R.menu.menu_executive_circle_member_settings)
        }

        popupMenu.setOnMenuItemClickListener(circleMemberOverflowMenuItemClickListener(context, item))
        Utils.changeMenuItemTextColor(
            popupMenu.menu.findItem(R.id.ban_member),
            ContextCompat.getColor(context, R.color.error),
        )

        popupMenu.show()
    }

    private fun circleMemberOverflowMenuItemClickListener(
        context: Context,
        member: MemberModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.edit_member_role -> {
                CircleMemberRoleEditAlertDialog(
                    context,
                    glideProvider,
                    member,
                    positiveListenerInitializer =
                        object : ItemListenerInitializer<Role> {
                            override fun initialize(item: Role) {
                                viewModel.editCircleMemberRole(member, item)
                            }

                            override fun initialize(
                                item: Role,
                                view: View?,
                            ) {}
                        },
                ).show()
            }

            R.id.ban_member -> {
                ContentDeleteAlertDialog(context, MESSAGE_BAN_MEMBER, BUTTON_NAME_BAN_MEMBER) {
                    viewModel.banCircleMember(member)
                }.show()
            }
        }
        true
    }

    private fun initJoinRequestedMemberView(context: Context) {
        binding.rvMember.adapter =
            JoinRequestedMemberAdapter(
                context,
                glideProvider,
                positiveListenerInitializer =
                    object : ItemListenerInitializer<MemberModel> {
                        override fun initialize(item: MemberModel) {
                            viewModel.acceptJoinRequest(item)
                        }

                        override fun initialize(
                            item: MemberModel,
                            view: View?,
                        ) {}
                    },
                negativeListenerInitializer =
                    object : ItemListenerInitializer<MemberModel> {
                        override fun initialize(item: MemberModel) {
                            viewModel.rejectJoinRequest(item)
                        }

                        override fun initialize(
                            item: MemberModel,
                            view: View?,
                        ) {}
                    },
            )
        binding.rvMember.layoutManager = LinearLayoutManager(context)
    }

    private fun initLeaveRequestedMemberView(context: Context) {
        binding.rvMember.adapter =
            LeaveRequestedMemberAdapter(
                context,
                glideProvider,
                showMessageListenerInitializer =
                    object : ItemListenerInitializer<MemberModel> {
                        override fun initialize(item: MemberModel) {
                            CircleMemberLeaveRequestAcceptAlertDialog(
                                context,
                                item,
                                object : ItemListenerInitializer<MemberModel> {
                                    override fun initialize(item: MemberModel) {
                                        viewModel.acceptLeaveRequest(item)
                                    }

                                    override fun initialize(
                                        item: MemberModel,
                                        view: View?,
                                    ) {}
                                },
                            )
                        }

                        override fun initialize(
                            item: MemberModel,
                            view: View?,
                        ) {}
                    },
            )
        binding.rvMember.layoutManager = LinearLayoutManager(context)
    }

    private fun hideBtmNav(parentActivity: Activity) {
        parentActivity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
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
        val loadingIndicator = parentActivity.findViewById<CircularProgressIndicator>(R.id.pgbLoading)
        loadingIndicator.isVisible = it is UiState.Loading
        when (it) {
            UiState.Success -> {
                requestRefreshToPreviousScreen()
                load(context, viewModel.members)
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

    private fun requestRefreshToPreviousScreen() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_CIRCLE_DATA_CHANGED, true)
    }

    private fun load(
        context: Context,
        members: MemberModels,
    ) {
        if (members.isEmpty()) {
            showNoMemberMessage(context)
        } else {
            loadMembers(members)
        }
    }

    private fun showNoMemberMessage(context: Context) {
        binding.txtNoMember.isVisible = true
        binding.rvMember.isVisible = false

        val messageId =
            if (membershipStatus == MembershipStatus.JOINED) {
                R.string.message_no_circle_member
            } else {
                R.string.message_no_member_request
            }
        binding.txtNoMember.text =
            ContextCompat.getString(context, messageId)
    }

    private fun loadMembers(members: MemberModels) {
        binding.rvMember.isVisible = true
        binding.txtNoMember.isVisible = false

        binding.rvMember.adapter?.let {
            when (membershipStatus) {
                MembershipStatus.JOINED -> {
                    (it as CircleMemberAdapter).update(members) {}
                }
                MembershipStatus.JOIN_REQUESTED -> {
                    (it as JoinRequestedMemberAdapter).update(members) {}
                }
                MembershipStatus.LEAVE_REQUESTED -> {
                    (it as LeaveRequestedMemberAdapter).update(members) {}
                }
                else -> {}
            }
        }
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

    private fun showErrorDialog(context: Context) {
        ErrorAlertDialog(context, viewModel.error).show()
    }

    companion object {
        private const val MESSAGE_BAN_MEMBER = "해당 멤버를 추방할까요?"
        private const val BUTTON_NAME_BAN_MEMBER = "추방"
    }
}
