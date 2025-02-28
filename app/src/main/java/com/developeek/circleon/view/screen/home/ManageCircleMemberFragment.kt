package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentManageCircleMemberBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CircleMemberAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManageCircleMemberFragment : Fragment() {
    private lateinit var binding: FragmentManageCircleMemberBinding
    private lateinit var members: MemberModels
    private lateinit var membershipStatus: MembershipStatus

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
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
    }

    private fun initView(
        parentActivity: Activity,
        context: Context,
    ) {
        initToolbar(context)
        initMemberRecyclerViewByMembershipStatus(context)
        hideBtmNav(parentActivity)

        if (members.isEmpty()) {
            showNoMemberMessage(context)
        } else {
            loadMembers()
        }
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
            else -> {}
        }
    }

    private fun initCircleMemberView(context: Context) {
        binding.rvMember.adapter =
            CircleMemberAdapter(
                context,
                glideProvider,
                overflowListenerInitializer =
                    object : ItemListenerInitializer<MemberModel> {
                        override fun initialize(item: MemberModel) {
                        }

                        override fun initialize(
                            item: MemberModel,
                            view: View?,
                        ) {}
                    },
            )
        binding.rvMember.layoutManager = LinearLayoutManager(context)
    }

    private fun showNoMemberMessage(context: Context) {
        binding.txtNoMember.isVisible = true

        val messageId =
            if (membershipStatus == MembershipStatus.JOINED) {
                R.string.message_no_circle_member
            } else {
                R.string.message_no_member_request
            }
        binding.txtNoMember.text =
            ContextCompat.getString(context, messageId)
    }

    private fun loadMembers() {
        binding.rvMember.isVisible = true

        binding.rvMember.adapter?.let {
            when (membershipStatus) {
                MembershipStatus.JOINED -> {
                    (it as CircleMemberAdapter).update(members) {}
                }
                else -> {}
            }
        }
    }

    private fun hideBtmNav(activity: Activity) {
        activity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }
}
