package com.developeek.circleon.view.screen.circle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentMyCircleBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.adapter.MyCircleAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.circle.MyCircleViewModel
import com.developeek.circleon.view.viewmodelimpl.circle.MyCircleScreen
import com.developeek.circleon.view.viewmodelimpl.circle.MyCircleViewModelImpl
import com.developeek.circleon.view.widget.PositiveAlertDialog
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyCircleFragment : Fragment() {
    private lateinit var binding: FragmentMyCircleBinding
    private lateinit var circles: Models<CircleSummaryModel>
    private lateinit var membershipStatus: MembershipStatus
    private val viewModel: MyCircleViewModel by viewModels<MyCircleViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<MyCircleViewModelImpl.MyCircleViewModelFactory> {
                    it.create(circles)
                }
        },
    )

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            circles = it.getSerializable(Const.TAG_CIRCLE_SUMMARIES) as Models<CircleSummaryModel>
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
        initListener(requireActivity())
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        handleEvent(it, requireActivity(), requireContext())
                    }
                }
                launch {
                    viewModel.screenFlow.collect {
                        handleScreenFlow(it)
                    }
                }
            }
        }
    }

    private fun initView(
        parentActivity: Activity,
        context: Context,
    ) {
        initToolbar(context)
        initCircleRecyclerView(parentActivity, context)
    }

    private fun initToolbar(context: Context) {
        val title =
            when (membershipStatus) {
                MembershipStatus.JOINED ->
                    context.getString(
                        R.string.circle_content_title_my_circle,
                    )
                MembershipStatus.JOIN_REQUESTED ->
                    context.getString(
                        R.string.circle_content_title_join_requested_circle,
                    )
                MembershipStatus.LEAVE_REQUESTED ->
                    context.getString(
                        R.string.circle_content_title_leave_requested_circle,
                    )
                else -> Const.EMPTY_TEXT
            }

        binding.txtTitleCircles.text = title
    }

    private fun loadNoCircleView() {
        if (binding.rvCircles.isVisible) binding.rvCircles.isVisible = false

        when (membershipStatus) {
            MembershipStatus.JOINED -> binding.llNoMyCircles.isVisible = true
            MembershipStatus.JOIN_REQUESTED -> binding.llNoJoinRequestedCircles.isVisible = true
            MembershipStatus.LEAVE_REQUESTED -> binding.llNoLeaveRequestedCircles.isVisible = true
            else -> {}
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
                            sendUserToCircleDetailScreen(parentActivity, item.circleId, item.name)
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
                                showCancelJoinRequestDialog(item, context)
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

    private fun showCancelJoinRequestDialog(
        circle: CircleSummaryModel,
        context: Context,
    ) {
        PositiveAlertDialog(
            context,
            context.getString(R.string.message_cancel_join_request),
            positiveListener = {
                viewModel.cancelJoinRequest(circle.memberId)
            },
        ).show()
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
        findNavController().popBackStack()
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

    private fun showDialog(
        event: Event.ShowDialog,
        context: Context,
    ) {
        SingleMessageAlertDialog(context, event.message).show()
    }

    private fun handleScreenFlow(screenFlow: MyCircleScreen) {
        binding.pgbLoading.isVisible = screenFlow is MyCircleScreen.LoadingView
        when (screenFlow) {
            is MyCircleScreen.SuccessView -> showSuccessView(screenFlow)
            else -> {}
        }
    }

    private fun showSuccessView(screenFlow: MyCircleScreen.SuccessView) {
        if (screenFlow.circles.isEmpty()) {
            loadNoCircleView()
            return
        }

        loadCirclesAndDoAfter(
            screenFlow.circles,
            after = {
                binding.rvCircles.isVisible = true
            },
        )
    }

    private fun loadCirclesAndDoAfter(
        circles: Models<CircleSummaryModel>,
        after: () -> Unit,
    ) {
        (binding.rvCircles.adapter as MyCircleAdapter).update(circles) {
            after()
        }
    }
}
