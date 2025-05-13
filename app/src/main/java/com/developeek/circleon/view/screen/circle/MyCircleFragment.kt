package com.developeek.circleon.view.screen.circle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentMyCircleBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.MyCircleAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.circle.MyCircleViewModel
import com.developeek.circleon.view.viewmodelimpl.circle.MyCircleViewModelImpl
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyCircleFragment : Fragment() {
    private lateinit var binding: FragmentMyCircleBinding
    private lateinit var circles: CircleSummaryModels
    private lateinit var membershipStatus: MembershipStatus
    private val viewModel: MyCircleViewModel by viewModels<MyCircleViewModelImpl>()

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circles = it.getSerializable(Const.TAG_CIRCLE_SUMMARIES) as CircleSummaryModels
            membershipStatus = it.getSerializable(Const.TAG_MEMBERSHIP_STATUS) as MembershipStatus
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyCircleBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireActivity(), requireContext())
        initObserver(requireActivity(), requireContext())
        initListener(requireActivity())
    }

    private fun initView(
        parentActivity: Activity,
        context: Context,
    ) {
        initToolbar(context)
        initCircleRecyclerView(parentActivity, context)

        if (circles.isEmpty()) {
            loadNoCircleView()
        } else {
            loadCircles(circles)
        }
    }

    private fun initToolbar(context: Context) {
        if (membershipStatus == MembershipStatus.JOINED) {
            binding.txtTitleCircles.text =
                ContextCompat.getString(context, R.string.circle_content_title_my_circle)
        }
        if (membershipStatus == MembershipStatus.JOIN_REQUESTED) {
            binding.txtTitleCircles.text =
                ContextCompat.getString(context, R.string.circle_content_title_join_requested_circle)
        }
        if (membershipStatus == MembershipStatus.LEAVE_REQUESTED) {
            binding.txtTitleCircles.text =
                ContextCompat.getString(context, R.string.circle_content_title_leave_requested_circle)
        }
    }

    private fun loadNoCircleView() {
        if (binding.rvCircles.isVisible) binding.rvCircles.isVisible = false
        if (membershipStatus == MembershipStatus.JOINED) {
            binding.llNoMyCircles.isVisible = true
        }
        if (membershipStatus == MembershipStatus.JOIN_REQUESTED) {
            binding.llNoJoinRequestedCircles.isVisible = true
        }
        if (membershipStatus == MembershipStatus.LEAVE_REQUESTED) {
            binding.llNoLeaveRequestedCircles.isVisible = true
        }
    }

    private fun initCircleRecyclerView(
        parentActivity: Activity,
        context: Context,
    ) {
        binding.rvCircles.adapter =
            MyCircleAdapter(
                context,
                glideProvider,
                membershipStatus,
                itemListenerInitializer =
                    object : ItemListenerInitializer<CircleSummaryModel> {
                        override fun initialize(item: CircleSummaryModel) {
                            sendUserToCircleDetailScreen(parentActivity, item.id, item.name)
                        }

                        override fun initialize(
                            item: CircleSummaryModel,
                            view: View?,
                        ) {}
                    },
                overflowListenerInitializer =
                    object : ItemListenerInitializer<CircleSummaryModel> {
                        override fun initialize(item: CircleSummaryModel) {
                            if (membershipStatus.isJoinRequested()) {
                                viewModel.cancelJoinRequest(item.memberId)
                            }
                        }

                        override fun initialize(
                            item: CircleSummaryModel,
                            view: View?,
                        ) {}
                    },
            )
        binding.rvCircles.layoutManager = LinearLayoutManager(context)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            binding.rvCircles.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun sendUserToCircleDetailScreen(
        parentActivity: Activity,
        circleId: Int,
        circleName: String,
    ) {
        selectHomeTab(parentActivity)
        findNavController().popBackStack(R.id.homeFragment, inclusive = false)
        findNavController().navigate(
            R.id.action_homeFragment_to_circleDetailFragment,
            bundleOf(
                Pair(Const.TAG_CIRCLE_ID, circleId),
                Pair(Const.TAG_CIRCLE_NAME, circleName),
            ),
        )
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
                if (viewModel.joinRequestedCircles.isEmpty()) {
                    loadNoCircleView()
                } else {
                    loadCircles(viewModel.joinRequestedCircles)
                }
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

    private fun loadCircles(circles: CircleSummaryModels) {
        if (!binding.rvCircles.isVisible) binding.rvCircles.isVisible = true
        (binding.rvCircles.adapter as MyCircleAdapter).update(circles) {}
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showErrorDialog(context: Context) {
        SingleMessageAlertDialog(context, viewModel.error).show()
    }

    private fun showErrorToast(context: Context) {
        if (SingleMessageToast.previousFinished()) {
            SingleMessageToast(context, viewModel.error).show()
        }
    }

    private fun initListener(parentActivity: Activity) {
        setBtnBackListener()
        setBtnMoveToSearchCircleScreenListener(parentActivity)
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().navigateUp()
    }

    private fun setBtnMoveToSearchCircleScreenListener(parentActivity: Activity) {
        binding.btnMoveToSearchCircleScreen.setOnClickListener {
            selectHomeTab(parentActivity)
            findNavController().popBackStack(R.id.homeFragment, inclusive = false)
        }
    }

    private fun selectHomeTab(parentActivity: Activity) {
        parentActivity.findViewById<BottomNavigationView>(R.id.btmNav).selectedItemId = R.id.nav_graph_home
    }
}
