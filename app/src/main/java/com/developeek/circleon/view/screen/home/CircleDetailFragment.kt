package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentCircleDetailBinding
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.CircleDetailViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailBinding
    private lateinit var fragmentManager: FragmentManager
    private val viewModel: CircleDetailViewModel by viewModels<CircleDetailViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<CircleDetailViewModelImpl.CircleDetailViewModelFactory> {
                    it.create(circleId)
                }
        },
    )
    private var circleId: Int = 0
    private lateinit var circleName: String

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

        initView()
        initObserver(requireActivity())
        initListener()
    }

    private fun initView() {
        binding.txtTbCircleName.text = circleName
        binding.txtCircleName.text = circleName
        binding.tbCircleDetail.inflateMenu(R.menu.menu_circle_settings)
    }

    private fun initObserver(activity: Activity) {
        viewModel.state.observe(
            viewLifecycleOwner,
            stateObserver(activity),
        )
    }

    private fun stateObserver(activity: Activity) =
        Observer<UiState> {
            when (it) {
                UiState.Loading -> {
                    toggleView(binding.pgbLoading)
                }
                UiState.Success -> {
                    toggleView(binding.flCircleDetail)
                    loadCircleDetail(activity)
                }
                UiState.AuthenticationError -> {
                    sendUserToLoginScreen(activity)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
                UiState.ServiceError -> {
                    toggleView(binding.llServiceError)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
            }
        }

    private fun loadCircleDetail(activity: Activity) {
        binding.tlCircleDetail.getTabAt(viewModel.currentTabPosition)?.select() // 탭 복원
        viewModel.circleDetail.thumbnailUrl?.let {
            glideProvider.callImage(it, activity, binding.imgCircleThumbnail)
        }
        binding.txtCircleCategory.text = viewModel.circleDetail.category.categoryName()
        binding.txtCircleMemberCount.text = String.format(MEMBER_COUNT_UNIT, viewModel.circleDetail.memberCount)
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun initListener() {
        setBtnBackListener()
        setTlCircleDetailListener()
        setBtnRetryListener()
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setTlCircleDetailListener() {
        binding.tlCircleDetail.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    viewModel.setTabPosition(tab?.position!!)
                    replaceByTabPosition()
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabReselected(p0: TabLayout.Tab?) {
                    resetScrollByTabPosition()
                }
            },
        )
    }

    private fun replaceByTabPosition() {
        val bundle = Bundle()

        when (viewModel.currentTabPosition) {
            0 -> {
                bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, viewModel.circleDetail)
                replaceTo(CircleDetailIntroductionFragment(), bundle)

                removeNotMemberViewIfVisible()
            }
            1 -> {
                if (viewModel.circleDetail.isMember()) {
                    bundle.putInt(Const.TAG_CIRCLE_ID, viewModel.circleDetail.id)
                    replaceTo(CircleDetailNoticeFragment(), bundle)
                } else if (!binding.llNotMember.isVisible) {
                    binding.llNotMember.isVisible = true
                }
            }
            2 -> {
                if (viewModel.circleDetail.isMember()) {
                    bundle.putInt(Const.TAG_CIRCLE_ID, viewModel.circleDetail.id)
                    replaceTo(CircleDetailPostFragment(), bundle)
                } else if (!binding.llNotMember.isVisible) {
                    binding.llNotMember.isVisible = true
                }
            }
            3 -> {
                replaceTo(CircleDetailActivityPhotoFragment())

                removeNotMemberViewIfVisible()
            }
        }
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
            .commit()
    }

    private fun resetScrollByTabPosition() {
        val fragment: Fragment
        val bundle = Bundle()

        when (viewModel.currentTabPosition) {
            0 -> {
                bundle.putSerializable(Const.TAG_CIRCLE_DETAIL, viewModel.circleDetail)
                replaceTo(CircleDetailIntroductionFragment(), bundle)
            }
            1 -> {
                fragment = childFragmentManager.findFragmentById(R.id.flCircleDetail) as CircleDetailNoticeFragment

                if (fragment.isAdded) {
                    fragment.view?.findViewById<RecyclerView>(R.id.rvCircleNotice)?.scrollToPosition(0)
                }
            }
            2 -> {
                fragment = childFragmentManager.findFragmentById(R.id.flCircleDetail) as CircleDetailPostFragment

                if (fragment.isAdded) {
                    fragment.view?.findViewById<RecyclerView>(R.id.rvCirclePost)?.scrollToPosition(0)
                }
            }
        }
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.load()
        }
    }

    private fun toggleView(view: View) {
        binding.flCircleDetail.visibility = visibleWhenTrue(view == binding.flCircleDetail)
        binding.pgbLoading.visibility = visibleWhenTrue(view == binding.pgbLoading)
        binding.llServiceError.visibility = visibleWhenTrue(view == binding.llServiceError)
    }

    private fun removeNotMemberViewIfVisible() {
        if (binding.llNotMember.isVisible) {
            binding.llNotMember.isVisible = false
        }
    }

    private fun visibleWhenTrue(state: Boolean) = if (state) View.VISIBLE else View.GONE

    companion object {
        private const val MEMBER_COUNT_UNIT = "멤버 %d명"
    }
}
