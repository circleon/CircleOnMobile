package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentManageCircleMemberBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CircleMemberAdapter
import com.developeek.circleon.view.adapter.JoinRequestedMemberAdapter
import com.developeek.circleon.view.adapter.LeaveRequestedMemberAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.screen.base.BaseFragment
import com.developeek.circleon.view.viewmodel.home.ManageCircleMemberViewModel
import com.developeek.circleon.view.viewmodelimpl.home.ManageCircleMemberScreen
import com.developeek.circleon.view.viewmodelimpl.home.ManageCircleMemberViewModelImpl
import com.developeek.circleon.view.widget.CircleMemberRequestAlertDialog
import com.developeek.circleon.view.widget.CircleMemberRoleEditAlertDialog
import com.developeek.circleon.view.widget.PositiveAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ManageCircleMemberFragment : BaseFragment() {
    private val binding: FragmentManageCircleMemberBinding by lazy {
        FragmentManageCircleMemberBinding.inflate(layoutInflater)
    }
    private val viewModel: ManageCircleMemberViewModel by viewModels<ManageCircleMemberViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<ManageCircleMemberViewModelImpl.ManageCircleMemberViewModelFactory> {
                    it.create(circle, members)
                }
        },
    )
    private lateinit var circle: CircleDetailModel
    private lateinit var members: Models<MemberModel>
    private lateinit var membershipStatus: MembershipStatus

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circle = it.getSerializable(Const.TAG_CIRCLE_DETAIL) as CircleDetailModel
            members = it.getSerializable(Const.TAG_MEMBERS) as Models<MemberModel>
            membershipStatus = it.getSerializable(Const.TAG_MEMBERSHIP_STATUS) as MembershipStatus
        }
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

        initView(requireActivity(), requireContext())
        initListener()
        handleEventFlow(requireContext())
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
                    context.getString(R.string.manage_circle_title_circle_member)
                MembershipStatus.JOIN_REQUESTED ->
                    context.getString(R.string.manage_circle_title_approve_circle_join)
                MembershipStatus.LEAVE_REQUESTED ->
                    context.getString(R.string.manage_circle_title_approve_circle_leave)
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
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvMember.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun initProfileOverflowMenuAndShow(
        context: Context,
        item: MemberModel,
        view: View,
    ) {
        val popupMenu = object : PopupMenu(context, view) {}

        inflatePopupMenuByUserRole(popupMenu, circle, context)
        popupMenu.setOnMenuItemClickListener(circleMemberOverflowMenuItemClickListener(context, item))
        popupMenu.show()
    }

    private fun inflatePopupMenuByUserRole(
        popupMenu: PopupMenu,
        circle: CircleDetailModel,
        context: Context,
    ) {
        if (circle.isUserPresident()) {
            popupMenu.inflate(R.menu.menu_president_circle_member_settings)
            Utils.changeMenuItemTextColor(
                popupMenu.menu.findItem(R.id.ban_member),
                ContextCompat.getColor(context, R.color.error),
            )
        } else if (circle.isUserExecutive()) {
            popupMenu.inflate(R.menu.menu_executive_circle_member_settings)
            Utils.changeMenuItemTextColor(
                popupMenu.menu.findItem(R.id.ban_member),
                ContextCompat.getColor(context, R.color.error),
            )
        }
    }

    private fun circleMemberOverflowMenuItemClickListener(
        context: Context,
        member: MemberModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.edit_member_role -> {
                showEditMemberRoleDialog(member, context)
            }

            R.id.ban_member -> {
                showBanMemberDialog(member, context)
            }
        }
        true
    }

    private fun showEditMemberRoleDialog(
        member: MemberModel,
        context: Context,
    ) {
        CircleMemberRoleEditAlertDialog(
            context,
            glideProvider,
            member,
            positiveListenerInitializer =
                object : ItemListenerInitializer<Role> {
                    override fun initialize(item: Role) {
                        requestRefreshToPreviousScreen()
                        viewModel.editCircleMemberRole(member, item)
                    }

                    override fun initialize(
                        item: Role,
                        view: View?,
                    ) {}
                },
        ).show()
    }

    private fun showBanMemberDialog(
        member: MemberModel,
        context: Context,
    ) {
        PositiveAlertDialog(
            context,
            String.format(context.getString(R.string.message_request_ban_member), member.name),
            context.getString(R.string.btn_ban_member),
            positiveListener = {
                requestRefreshToPreviousScreen()
                viewModel.banCircleMember(member)
            },
        ).show()
    }

    private fun initJoinRequestedMemberView(context: Context) {
        binding.rvMember.adapter =
            JoinRequestedMemberAdapter(
                context,
                glideProvider,
                showMessageListenerInitializer =
                    object : ItemListenerInitializer<MemberModel> {
                        override fun initialize(item: MemberModel) {
                            showAcceptJoinRequestDialog(item, context)
                        }

                        override fun initialize(
                            item: MemberModel,
                            view: View?,
                        ) {}
                    },
            )
        binding.rvMember.layoutManager = LinearLayoutManager(context)
    }

    private fun showAcceptJoinRequestDialog(
        member: MemberModel,
        context: Context,
    ) {
        CircleMemberRequestAlertDialog(
            context,
            title = context.getString(R.string.title_member_message_dialog_for_join_accept),
            member = member,
            positiveButton = context.getString(R.string.btn_accept_join_request),
            negativeButton = context.getString(R.string.btn_reject_join_request),
            negativeListenerInitializer =
                object : ItemListenerInitializer<MemberModel> {
                    override fun initialize(item: MemberModel) {
                        requestRefreshToPreviousScreen()
                        viewModel.rejectJoinRequest(item)
                    }

                    override fun initialize(
                        item: MemberModel,
                        view: View?,
                    ) {}
                },
            positiveListenerInitializer =
                object : ItemListenerInitializer<MemberModel> {
                    override fun initialize(item: MemberModel) {
                        requestRefreshToPreviousScreen()
                        viewModel.acceptJoinRequest(item)
                    }

                    override fun initialize(
                        item: MemberModel,
                        view: View?,
                    ) {}
                },
        ).show()
    }

    private fun initLeaveRequestedMemberView(context: Context) {
        binding.rvMember.adapter =
            LeaveRequestedMemberAdapter(
                context,
                glideProvider,
                showMessageListenerInitializer =
                    object : ItemListenerInitializer<MemberModel> {
                        override fun initialize(item: MemberModel) {
                            showAcceptLeaveRequestDialog(item, context)
                        }

                        override fun initialize(
                            item: MemberModel,
                            view: View?,
                        ) {}
                    },
            )
        binding.rvMember.layoutManager = LinearLayoutManager(context)
    }

    private fun showAcceptLeaveRequestDialog(
        member: MemberModel,
        context: Context,
    ) {
        CircleMemberRequestAlertDialog(
            context,
            title = context.getString(R.string.title_member_message_dialog_for_leave_accept),
            member = member,
            positiveButton = context.getString(R.string.btn_accept_leave_request),
            negativeListenerInitializer =
                object : ItemListenerInitializer<MemberModel> {
                    override fun initialize(item: MemberModel) {}

                    override fun initialize(
                        item: MemberModel,
                        view: View?,
                    ) {}
                },
            positiveListenerInitializer =
                object : ItemListenerInitializer<MemberModel> {
                    override fun initialize(item: MemberModel) {
                        requestRefreshToPreviousScreen()
                        viewModel.acceptLeaveRequest(item)
                    }

                    override fun initialize(
                        item: MemberModel,
                        view: View?,
                    ) {}
                },
        ).show()
    }

    private fun hideBtmNav(parentActivity: Activity) {
        parentActivity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    private fun initListener() {
        setBtnBackListener()
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().popBackStack()
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
                        handleScreenFlow(it, context)
                    }
                }
            }
        }
    }

    override fun showProcessing() {
        binding.pgbLoading.isVisible = true
    }

    override fun endProcessing() {
        binding.pgbLoading.isVisible = false
    }

    private fun handleScreenFlow(
        screenFlow: ManageCircleMemberScreen,
        context: Context,
    ) {
        binding.pgbLoading.isVisible = screenFlow is ManageCircleMemberScreen.Loading
        when (screenFlow) {
            is ManageCircleMemberScreen.Success -> showSuccessView(screenFlow, context)
            else -> {}
        }
    }

    private fun showSuccessView(
        screenFlow: ManageCircleMemberScreen.Success,
        context: Context,
    ) {
        load(screenFlow.members, context)
    }

    private fun requestRefreshToPreviousScreen() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_CIRCLE_DATA_CHANGED, true)
    }

    private fun load(
        members: Models<MemberModel>,
        context: Context,
    ) {
        if (members.isEmpty()) {
            showNoMemberMessage(context)
        } else {
            loadMembers(members)
        }
    }

    private fun showNoMemberMessage(context: Context) {
        switchView(binding.llNoMember)

        when (membershipStatus) {
            MembershipStatus.JOINED -> {
                binding.txtNoMember.text = context.getString(R.string.message_no_circle_member)
                binding.icSituation.setImageResource(R.drawable.character_bad_situation)
            }
            MembershipStatus.JOIN_REQUESTED -> {
                binding.txtNoMember.text = context.getString(R.string.message_no_member_join_requested)
                binding.icSituation.setImageResource(R.drawable.character_bad_situation)
            }
            MembershipStatus.LEAVE_REQUESTED -> {
                binding.txtNoMember.text = context.getString(R.string.message_no_member_leave_requested)
                binding.icSituation.setImageResource(R.drawable.character_good_situation)
            }
            else -> {}
        }
    }

    private fun loadMembers(members: Models<MemberModel>) {
        switchView(binding.rvMember)

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

    private fun switchView(view: View) {
        binding.rvMember.isVisible = view == binding.rvMember
        binding.llNoMember.isVisible = view == binding.llNoMember
    }
}
