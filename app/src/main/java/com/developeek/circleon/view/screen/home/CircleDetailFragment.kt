package com.developeek.circleon.view.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentCircleDetailBinding
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.CircleDetailViewModel
import com.developeek.circleon.view.viewmodelimpl.CircleDetailViewModelImpl
import com.developeek.circleon.view.widget.ErrorToast
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CircleDetailFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailBinding
    private lateinit var fragmentManager: FragmentManager
    private val viewModel: CircleDetailViewModel by viewModels<CircleDetailViewModelImpl>()
    private var circleId: Int = 0
    private lateinit var circleName: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailBinding.inflate(layoutInflater)
        fragmentManager = requireActivity().supportFragmentManager
        arguments?.let {
            circleId = it.getInt(Const.TAG_CIRCLE_ID)
            circleName = it.getString(Const.TAG_CIRCLE_NAME) ?: Const.EMPTY_TEXT
        }

        viewModel.load(circleId)

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
                    add(CircleDetailIntroductionFragment(viewModel.circleDetail))
                    loadCircleDetail()
                }
                UiState.RefreshExpiration -> {
                    sendUserToLoginScreen(activity)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
                UiState.Error -> {
                    toggleView(binding.llServiceError)
                    if (ErrorToast.previousFinished()) {
                        ErrorToast(activity, viewModel.error).show()
                    }
                }
            }
        }

    private fun loadCircleDetail() {
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
                    when (tab?.position) {
                        0 -> {
                            replaceTo(CircleDetailIntroductionFragment(viewModel.circleDetail))
                        }
                        1 -> {
                            replaceTo(CircleDetailNoticeFragment())
                        }
                        2 -> {
                            replaceTo(CircleDetailPostFragment())
                        }
                        3 -> {
                            replaceTo(CircleDetailActivityPhotoFragment())
                        }
                    }
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabReselected(p0: TabLayout.Tab?) {
                }
            },
        )
    }

    private fun setBtnRetryListener() {
        binding.btnRetry.setOnClickListener {
            viewModel.load(circleId)
        }
    }

    private fun add(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.add(binding.flCircleDetail.id, fragment)
        transaction.commit()
    }

    private fun replaceTo(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(binding.flCircleDetail.id, fragment)
        transaction.commit()
    }

    private fun toggleView(view: View) {
        binding.flCircleDetail.visibility = visibleWhenTrue(view == binding.flCircleDetail)
        binding.pgbLoading.visibility = visibleWhenTrue(view == binding.pgbLoading)
        binding.llServiceError.visibility = visibleWhenTrue(view == binding.llServiceError)
    }

    private fun visibleWhenTrue(state: Boolean) = if (state) View.VISIBLE else View.GONE

    companion object {
        private const val MEMBER_COUNT_UNIT = "멤버 %d명"
    }
}
