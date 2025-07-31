package com.developeek.circleon.view.screen.home

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentCircleDetailBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.screen.base.BaseFragment
import com.developeek.circleon.view.viewmodel.home.CircleDetailViewModel
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailScreen
import com.developeek.circleon.view.viewmodelimpl.home.CircleDetailViewModelImpl
import com.developeek.circleon.view.viewmodelimpl.home.SelectTab
import com.developeek.circleon.view.widget.CircleRequestAlertDialog
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailFragment : BaseFragment() {
    private val binding: FragmentCircleDetailBinding by lazy {
        FragmentCircleDetailBinding.inflate(layoutInflater)
    }
    private lateinit var fragmentManager: FragmentManager
    private val viewModel: CircleDetailViewModel by viewModels<CircleDetailViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<CircleDetailViewModelImpl.CircleDetailViewModelFactory> {
                    it.create(circleId)
                }
        },
    )
    private var circleId = 0
    private var circleName = Const.EMPTY_TEXT

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 런타임에서 뷰모델에 circleId 를 전달하기 위해 onCreate 에서 초기화
        arguments?.let {
            circleId = it.getInt(Const.TAG_CIRCLE_ID)
            circleName = it.getString(Const.TAG_CIRCLE_NAME) ?: Const.EMPTY_TEXT
        }
        fragmentManager = childFragmentManager
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

        initView()
        initListener()
        initRefreshObserver()
        handleEventFlow(requireContext())
    }

    private fun initView() {
        initAppBar()
    }

    private fun initAppBar() {
        binding.abCircleDetail.setExpanded(viewModel.currentAppBarExpanded)
    }

    private fun initListener() {
        setBtnBackListener()
        setAbCircleDetailListener()
        setTlCircleDetailListener()
        setBtnRetryListener()
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().popBackStack()
    }

    private fun setAbCircleDetailListener() {
        binding.abCircleDetail.addOnOffsetChangedListener { _, offset ->
            if (viewModel.currentAppBarExpanded && offset != 0) {
                viewModel.setAppBarExpanded(false)
            } else if (!viewModel.currentAppBarExpanded && offset == 0) {
                viewModel.setAppBarExpanded(true)
            }
        }
    }

    private fun setTlCircleDetailListener() {
        binding.tlCircleDetail.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    viewModel.selectTab(tab?.position!!, false)
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    viewModel.selectTab(tab?.position!!, true)
                }
            },
        )
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun initRefreshObserver() {
        // 정보 수정 여부 감지
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.let {
                it.getLiveData<Boolean>(Const.FLAG_CIRCLE_DATA_CHANGED)
                    .observe(viewLifecycleOwner) { dataChanged ->
                        if (dataChanged) {
                            viewModel.refresh()
                            it[Const.FLAG_CIRCLE_DATA_CHANGED] = false
                        }
                    }
            }
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
                launch {
                    viewModel.tabFlow.collect {
                        selectTab(it)
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
        screenFlow: CircleDetailScreen,
        context: Context,
    ) {
        binding.pgbLoading.isVisible = screenFlow is CircleDetailScreen.Loading
        when (screenFlow) {
            is CircleDetailScreen.Success -> showSuccessView(screenFlow, context)
            is CircleDetailScreen.Error -> showErrorView()
            else -> {}
        }
    }

    private fun showSuccessView(
        screenFlow: CircleDetailScreen.Success,
        context: Context,
    ) {
        switchView(binding.flCircleDetail)
        loadCurrentTab(viewModel.currentTabPosition)
        screenFlow.circleDetail.let {
            loadCircleDetail(context, it)
            setBtnCircleDetailListener(context, it)
        }
    }

    private fun loadCurrentTab(tabPosition: Int) {
        binding.tlCircleDetail.getTabAt(tabPosition)?.select()
    }

    private fun loadCircleDetail(
        context: Context,
        circleDetail: CircleDetailModel,
    ) {
        circleDetail.let {
            binding.txtTbCircleName.text = it.name
            binding.txtCircleName.text = it.name
            binding.icOfficial.isVisible = it.isOfficial()
            it.thumbnailUrl?.let {
                glideProvider.fetchImage(it, context, binding.imgCircleThumbnail)
            } ?: binding.imgCircleThumbnail.setImageResource(R.drawable.img_circle_profile_default)
            binding.txtCircleCategory.text = it.category.categoryName
            binding.txtCircleMemberCount.text =
                Html.fromHtml(
                    String.format(
                        ContextCompat.getString(context, R.string.underlined_number), it.memberCount,
                    ),
                    Html.FROM_HTML_MODE_LEGACY,
                )
        }
    }

    private fun setBtnCircleDetailListener(
        context: Context,
        circleDetail: CircleDetailModel,
    ) {
        setBtnCircleOverflowListener(context, circleDetail)
        setBtnMemberCountListener(circleDetail)
        setBtnRequestJoinCircle(context, circleDetail)
    }

    private fun setBtnCircleOverflowListener(
        context: Context,
        circleDetail: CircleDetailModel,
    ) {
        binding.btnCircleOverflow.setOnClickListener {
            showPopupMenuByUserRole(context, circleDetail)
        }
    }

    private fun showPopupMenuByUserRole(
        context: Context,
        circleDetail: CircleDetailModel,
    ) {
        val popupMenu = object : PopupMenu(context, binding.btnCircleOverflow) {}

        inflatePopupByUserRole(context, popupMenu, circleDetail.role)
        popupMenu.setOnMenuItemClickListener(circleOverflowMenuItemClickListener(context, circleDetail))
        popupMenu.show()
    }

    private fun inflatePopupByUserRole(
        context: Context,
        popupMenu: PopupMenu,
        role: Role,
    ) {
        when (role) {
            Role.NONE_MEMBER -> {
                popupMenu.inflate(R.menu.menu_non_member_circle_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.report_circle),
                    ContextCompat.getColor(context, R.color.error),
                )
            }
            Role.MEMBER -> {
                popupMenu.inflate(R.menu.menu_member_circle_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.leave_circle),
                    ContextCompat.getColor(context, R.color.error),
                )
            }
            Role.EXECUTIVE -> {
                popupMenu.inflate(R.menu.menu_executive_circle_settings)
                Utils.changeMenuItemTextColor(
                    popupMenu.menu.findItem(R.id.leave_circle),
                    ContextCompat.getColor(context, R.color.error),
                )
            }
            Role.PRESIDENT -> {
                popupMenu.inflate(R.menu.menu_president_circle_settings)
            }
        }
    }

    private fun circleOverflowMenuItemClickListener(
        context: Context,
        item: CircleDetailModel,
    ) = PopupMenu.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.edit_circle -> {
                sendUserToEditCircleScreen(item)
            }
            R.id.manage_circle -> {
                sendUserToManageCircleScreen(item)
            }
            R.id.leave_circle -> {
                if (item.membershipStatus.isLeaveRequested()) {
                    showAlreadyLeaveRequestedDialog(context)
                } else {
                    showLeaveRequestDialog(context)
                }
            }
            R.id.report_circle -> {
                showReportRequestDialog(context)
            }
        }
        true
    }

    private fun sendUserToEditCircleScreen(item: CircleDetailModel) {
        val bundle = Bundle()

        bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, item)
        bundle.putBoolean(Const.FLAG_EDIT_SCREEN, true) // 수정 기능 전용 활성화
        findNavController().navigate(R.id.action_circleDetailFragment_to_uploadCircleFragment, bundle)
    }

    private fun sendUserToManageCircleScreen(item: CircleDetailModel) {
        val bundle = Bundle()

        bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, item)
        findNavController().navigate(R.id.action_circleDetailFragment_to_manageCircleFragment, bundle)
    }

    private fun showAlreadyLeaveRequestedDialog(context: Context) {
        SingleMessageAlertDialog(context, context.getString(R.string.message_already_leave_requested)).show()
    }

    private fun showLeaveRequestDialog(context: Context) {
        CircleRequestAlertDialog(
            context,
            title = context.getString(R.string.title_member_leave_message_dialog),
            positiveButton = context.getString(R.string.btn_leave_request),
            positiveListenerInitializer =
                object : ItemListenerInitializer<String> {
                    override fun initialize(item: String) {
                        viewModel.requestLeave(item)
                    }

                    override fun initialize(
                        item: String,
                        view: View?,
                    ) {
                    }
                },
        ).show()
    }

    private fun showReportRequestDialog(context: Context) {
        CircleRequestAlertDialog(
            context,
            title = context.getString(R.string.title_report_dialog),
            positiveButton = context.getString(R.string.menu_report),
            positiveListenerInitializer =
                object : ItemListenerInitializer<String> {
                    override fun initialize(item: String) {
                        viewModel.requestReport(item)
                    }

                    override fun initialize(
                        item: String,
                        view: View?,
                    ) {}
                },
        ).show()
    }

    private fun setBtnMemberCountListener(circleDetail: CircleDetailModel) {
        binding.txtCircleMemberCount.setOnClickListener {
            sendUserToCircleMemberScreen(circleDetail)
        }
    }

    private fun sendUserToCircleMemberScreen(circleDetail: CircleDetailModel) {
        val bundle = Bundle()

        bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, circleDetail)
        bundle.putSerializable(Const.TAG_MEMBERS, circleDetail.members)
        bundle.putSerializable(Const.TAG_MEMBERSHIP_STATUS, MembershipStatus.JOINED)
        findNavController().navigate(R.id.action_circleDetailFragment_to_manageCircleMemberFragment, bundle)
    }

    private fun setBtnRequestJoinCircle(
        context: Context,
        circleDetail: CircleDetailModel,
    ) {
        binding.btnRequestJoinCircle.isVisible = !circleDetail.isUserJoined()

        if (circleDetail.membershipStatus.isNotJoined()) {
            binding.btnRequestJoinCircle.text = context.getString(R.string.btn_request_join_circle)
            binding.btnRequestJoinCircle.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple_5))
            binding.btnRequestJoinCircle.setOnClickListener {
                showRequestJoinDialog(context)
            }
        }
        if (circleDetail.membershipStatus.isJoinRequested()) {
            binding.btnRequestJoinCircle.text = context.getString(R.string.text_circle_join_requested)
            binding.btnRequestJoinCircle.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple_6))
            binding.btnRequestJoinCircle.isClickable = false
        }
    }

    private fun showRequestJoinDialog(context: Context) {
        CircleRequestAlertDialog(
            context,
            title = context.getString(R.string.title_member_join_message_dialog),
            positiveButton = context.getString(R.string.btn_join_request),
            positiveListenerInitializer =
                object : ItemListenerInitializer<String> {
                    override fun initialize(item: String) {
                        viewModel.requestJoin(item)
                    }

                    override fun initialize(
                        item: String,
                        view: View?,
                    ) {}
                },
        ).show()
    }

    private fun showErrorView() {
        switchView(binding.llServiceError)
    }

    private fun selectTab(event: SelectTab) {
        event.let {
            if (it.reselected) {
                resetScrollByTabPosition(it.circleDetail, it.tabPosition)
                return
            }

            replaceScreenByTabPosition(it.circleDetail, it.tabPosition)
            replaceFabContentByTabPosition(it.circleDetail, it.tabPosition)
        }
    }

    private fun resetScrollByTabPosition(
        circleDetail: CircleDetailModel,
        tabPosition: Int,
    ) {
        val bundle = Bundle()

        when (tabPosition) {
            0 -> {
                replaceToIntroductionScreen(circleDetail, bundle)
            }
            1 -> {
                resetScrollOfNoticeView()
            }
            2 -> {
                resetScrollOfPostView()
            }
        }
    }

    private fun resetScrollOfNoticeView() {
        childFragmentManager.findFragmentById(R.id.flCircleDetail)?.let {
            val fragment = it as CircleDetailNoticeFragment

            if (fragment.isAdded) {
                fragment.view?.findViewById<RecyclerView>(R.id.rvCircleNotice)?.scrollToPosition(0)
            }
        }
    }

    private fun resetScrollOfPostView() {
        childFragmentManager.findFragmentById(R.id.flCircleDetail)?.let {
            val fragment = it as CircleDetailPostFragment

            if (fragment.isAdded) {
                fragment.view?.findViewById<RecyclerView>(R.id.rvCirclePost)?.scrollToPosition(0)
            }
        }
    }

    private fun replaceScreenByTabPosition(
        circleDetail: CircleDetailModel,
        tabPosition: Int,
    ) {
        val bundle = Bundle()

        when (tabPosition) {
            0 -> {
                replaceToIntroductionScreen(circleDetail, bundle)
            }
            1 -> {
                replaceToNoticeScreen(circleDetail, bundle)
            }
            2 -> {
                replaceToPostScreen(circleDetail, bundle)
            }
            3 -> {
                replaceToPhotoScreen(bundle)
            }
        }
    }

    private fun replaceToIntroductionScreen(
        circleDetail: CircleDetailModel,
        bundle: Bundle,
    ) {
        bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, circleDetail)
        replaceToIfNotSame(CircleDetailIntroductionFragment(), bundle)
    }

    private fun replaceToNoticeScreen(
        circleDetail: CircleDetailModel,
        bundle: Bundle,
    ) {
        if (circleDetail.isUserJoined()) {
            bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, circleDetail)
            replaceToIfNotSame(CircleDetailNoticeFragment(), bundle)
        } else if (!binding.llNotMember.isVisible) {
            binding.llNotMember.isVisible = true
        }
    }

    private fun replaceToPostScreen(
        circleDetail: CircleDetailModel,
        bundle: Bundle,
    ) {
        if (circleDetail.isUserJoined()) {
            bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, circleDetail)
            replaceToIfNotSame(CircleDetailPostFragment(), bundle)
        } else if (!binding.llNotMember.isVisible) {
            binding.llNotMember.isVisible = true
        }
    }

    private fun replaceToPhotoScreen(bundle: Bundle) {
        replaceToIfNotSame(CircleDetailActivityPhotoFragment())
    }

    private fun replaceToIfNotSame(
        fragment: Fragment,
        bundle: Bundle? = null,
    ) {
        bundle?.let {
            fragment.arguments = bundle
        }

        fragmentManager.findFragmentByTag(fragment::class.java.simpleName)?.let {
            removeNotMemberViewIfVisible()
            return
        }

        val transaction = fragmentManager.beginTransaction()
        transaction
            .replace(binding.flCircleDetail.id, fragment, fragment::class.java.simpleName)
            .runOnCommit { removeNotMemberViewIfVisible() } // 비공개 뷰는 fragment 가 아니기 때문에 커밋 직후 별도로 전환
            .commit()
    }

    private fun replaceFabContentByTabPosition(
        circleDetail: CircleDetailModel,
        tabPosition: Int,
    ) {
        val bundle = Bundle()

        when (tabPosition) {
            0 -> {
                binding.fabUploadCircleContent.isVisible = false
            }
            1 -> {
                binding.fabUploadCircleContent.isVisible = circleDetail.isUserExecutive()
                binding.fabUploadCircleContent.setOnClickListener {
                    sendUserToUploadPostScreen(circleId, PostType.NOTICE, bundle)
                }
            }
            2 -> {
                binding.fabUploadCircleContent.isVisible = circleDetail.isUserJoined()
                binding.fabUploadCircleContent.setOnClickListener {
                    sendUserToUploadPostScreen(circleId, PostType.POST, bundle)
                }
            }
            3 -> {
                // TODO: 활동 사진 기능 추가 후 fab src 교체 및 리스너 설정
                binding.fabUploadCircleContent.isVisible = false
            }
        }
    }

    private fun sendUserToUploadPostScreen(
        circleId: Int,
        postType: PostType,
        bundle: Bundle,
    ) {
        bundle.putInt(Const.TAG_CIRCLE_ID, circleId)
        bundle.putSerializable(Const.TAG_POST_TYPE, postType)
        bundle.putBoolean(Const.FLAG_EDIT_SCREEN, false) // 신규 작성 전용 기능 활성화
        findNavController().navigate(R.id.action_circleDetailFragment_to_uploadPostFragment, bundle)
    }

    private fun switchView(view: View) {
        binding.flCircleDetail.isVisible = view == binding.flCircleDetail
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    private fun removeNotMemberViewIfVisible() {
        if (binding.llNotMember.isVisible) {
            binding.llNotMember.isVisible = false
        }
    }
}
