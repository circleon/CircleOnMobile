package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentCircleDetailBinding
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.CircleDetailViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailBinding
    private lateinit var fragmentManager: FragmentManager
    private var circleId: Int = 0
    private lateinit var circleName: String
    private val viewModel: CircleDetailViewModel by viewModels<CircleDetailViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<CircleDetailViewModelImpl.CircleDetailViewModelFactory> {
                    it.create(circleId)
                }
        },
    )

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 런타임에서 뷰모델에 circleId 를 전달하기 위해 onCreate 에서 초기화
        arguments?.let {
            circleId = it.getInt(Const.TAG_CIRCLE_ID)
            circleName = it.getString(Const.TAG_CIRCLE_NAME) ?: Const.EMPTY_TEXT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailBinding.inflate(layoutInflater)
        // 하위 fragment 에서 circleDetailFragment 를 부모로 인식하기 위해 childFragmentManager 사용
        fragmentManager = childFragmentManager

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireActivity())
        initObserver(requireActivity())
        initListener()
    }

    private fun initView(activity: Activity) {
        initAppBar()
        initOverflowMenu(activity)
        loadCircleDetailContent()
    }

    private fun initOverflowMenu(activity: Activity) {
        binding.tbCircleDetail.inflateMenu(R.menu.menu_circle_settings)
        Utils.changeMenuItemTextColor(
            binding.tbCircleDetail.menu.findItem(R.id.resign_circle),
            ContextCompat.getColor(activity, R.color.error),
        )
    }

    private fun sendUserToEditCircleScreen(item: CircleDetailModel) {
        val bundle = Bundle()

        bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, item)
        bundle.putBoolean(Const.FLAG_EDIT_OR_NOT, true) // 수정 기능 전용 활성화
        findNavController().navigate(R.id.action_circleDetailFragment_to_uploadCircleFragment, bundle)
    }

    private fun initAppBar() {
        binding.abCircleDetail.setExpanded(viewModel.currentAppBarExpanded)
    }

    private fun loadCircleDetailContent() {
        binding.txtTbCircleName.text = circleName
        binding.txtCircleName.text = circleName
    }

    private fun initObserver(activity: Activity) {
        viewModel.state.observe(
            viewLifecycleOwner,
            stateObserver(activity),
        )
    }

    private fun stateObserver(activity: Activity) =
        Observer<UiState> {
            val loadingIndicator = activity.findViewById<CircularProgressIndicator>(R.id.pgbLoading)
            loadingIndicator.isVisible = it is UiState.Loading
            when (it) {
                UiState.Success -> {
                    loadingIndicator.isVisible = false
                    toggleView(binding.flCircleDetail)
                    loadCircleDetail(activity)
                    binding.tbCircleDetail.setOnMenuItemClickListener(
                        circleOverflowMenuItemClickListener(activity, viewModel.circleDetail),
                    )
                }
                UiState.AuthenticationError -> {
                    loadingIndicator.isVisible = false
                    sendUserToLoginScreen(activity)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
                UiState.ServiceError -> {
                    loadingIndicator.isVisible = false
                    toggleView(binding.llServiceError)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
                else -> {}
            }
        }

    private fun loadCircleDetail(activity: Activity) {
        binding.tlCircleDetail.getTabAt(viewModel.currentTabPosition)?.select() // 탭 복원
        viewModel.circleDetail.thumbnailUrl?.let {
            glideProvider.fetchImage(it, activity, binding.imgCircleThumbnail)
        }
        binding.txtCircleCategory.text = viewModel.circleDetail.category.categoryName()
        binding.txtCircleMemberCount.text = String.format(MEMBER_COUNT_UNIT, viewModel.circleDetail.memberCount)
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun circleOverflowMenuItemClickListener(
        activity: Activity,
        item: CircleDetailModel,
    ) = Toolbar.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.edit_circle -> {
                sendUserToEditCircleScreen(item)
            }
            R.id.manage_circle -> {
            }
            R.id.resign_circle -> {
            }
        }
        true
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
        findNavController().navigateUp()
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
                    viewModel.setTabPosition(tab?.position!!)
                    if (viewModel.circleDetailInitialized) {
                        replaceScreenByTabPosition()
                        replaceFabContentByTabPosition()
                    }
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabReselected(p0: TabLayout.Tab?) {
                    if (viewModel.circleDetailInitialized) {
                        resetScrollByTabPosition()
                    }
                }
            },
        )
    }

    private fun replaceScreenByTabPosition() {
        val bundle = Bundle()

        when (viewModel.currentTabPosition) {
            0 -> {
                replaceToIntroductionScreen(bundle)
            }
            1 -> {
                replaceToNoticeScreen(bundle)
            }
            2 -> {
                replaceToPostScreen(bundle)
            }
            3 -> {
                replaceToPhotoScreen(bundle)
            }
        }
    }

    private fun replaceToIntroductionScreen(bundle: Bundle) {
        bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, viewModel.circleDetail)
        replaceTo(CircleDetailIntroductionFragment(), bundle)
    }

    private fun replaceToNoticeScreen(bundle: Bundle) {
        if (viewModel.circleDetail.isMember()) {
            bundle.putInt(Const.TAG_CIRCLE_ID, viewModel.circleDetail.id)
            bundle.putSerializable(Const.TAG_USER_ROLE, viewModel.circleDetail.role)
            replaceTo(CircleDetailNoticeFragment(), bundle)
        } else if (!binding.llNotMember.isVisible) {
            binding.llNotMember.isVisible = true
        }
    }

    private fun replaceToPostScreen(bundle: Bundle) {
        if (viewModel.circleDetail.isMember()) {
            bundle.putInt(Const.TAG_CIRCLE_ID, viewModel.circleDetail.id)
            replaceTo(CircleDetailPostFragment(), bundle)
        } else if (!binding.llNotMember.isVisible) {
            binding.llNotMember.isVisible = true
        }
    }

    private fun replaceToPhotoScreen(bundle: Bundle) {
        replaceTo(CircleDetailActivityPhotoFragment())
    }

    private fun replaceTo(
        fragment: Fragment,
        bundle: Bundle? = null,
    ) {
        bundle?.let {
            fragment.arguments = bundle
        }

        val transaction = fragmentManager.beginTransaction()
        transaction
            .replace(binding.flCircleDetail.id, fragment)
            .runOnCommit { removeNotMemberViewIfVisible() } // 비공개 뷰는 fragment 가 아니기 때문에 커밋 직후 별도로 전환
            .commit()
    }

    private fun replaceFabContentByTabPosition() {
        val bundle = Bundle()

        when (viewModel.currentTabPosition) {
            0 -> {
                binding.fabUploadCircleContent.isVisible = false
            }
            1 -> {
                binding.fabUploadCircleContent.isVisible = viewModel.circleDetail.isExecutive()
                binding.fabUploadCircleContent.setOnClickListener {
                    sendUserToUploadPostScreen(circleId, PostType.NOTICE, bundle)
                }
            }
            2 -> {
                binding.fabUploadCircleContent.isVisible = viewModel.circleDetail.isMember()
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
        bundle.putBoolean(Const.FLAG_EDIT_OR_NOT, false) // 신규 작성 전용 기능 활성화
        findNavController().navigate(R.id.action_circleDetailFragment_to_uploadPostFragment, bundle)
    }

    private fun resetScrollByTabPosition() {
        val bundle = Bundle()

        when (viewModel.currentTabPosition) {
            0 -> {
                replaceToIntroductionScreen(bundle)
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

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun toggleView(view: View) {
        binding.flCircleDetail.isVisible = view == binding.flCircleDetail
        binding.llServiceError.isVisible = view == binding.llServiceError
    }

    private fun removeNotMemberViewIfVisible() {
        if (binding.llNotMember.isVisible) {
            binding.llNotMember.isVisible = false
        }
    }

    companion object {
        private const val MEMBER_COUNT_UNIT = "멤버 %d명"
    }
}
