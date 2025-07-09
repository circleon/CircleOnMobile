package com.developeek.circleon.view.screen

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ActivityHomeBinding
import com.developeek.circleon.view.widget.SingleMessageToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var finishWaitingToast: Toast
    private var backClicked = false
    private var onMainFragment = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView(this)
    }

    private fun initView(context: Context) {
        initBottomNav()
        initFinishWaitingToast(context)
    }

    private fun initBottomNav() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.containerHome) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.btmNav, navController)
        setDestinationChangedListener()
        setItemReselectionListener()
    }

    private fun setDestinationChangedListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            onMainFragment = destination.id == R.id.homeFragment

            // fragment 가 초기화되기 전에 visibility 를 설정할 경우 뷰 리사이징으로 인해
            // UX 에 좋지 않기 때문에 btmNav Hide 작업은 각 fragment 내에서 진행
            if (!hideBtmNavCondition(destination) && !binding.btmNav.isVisible) {
                binding.btmNav.isVisible = true
            }
        }
    }

    private fun hideBtmNavCondition(destination: NavDestination) =
        destination.id == R.id.searchCircleFragment ||
            destination.id == R.id.uploadCircleFragment ||
            destination.id == R.id.manageCircleFragment ||
            destination.id == R.id.manageCircleMemberFragment ||
            destination.id == R.id.uploadPostFragment ||
            destination.id == R.id.circleDetailPostDetailFragment

    private fun setItemReselectionListener() {
        binding.btmNav.setOnItemReselectedListener {
            navController.currentDestination?.parent?.let {
                if (it.startDestinationId != R.id.nav_graph_home) { // 탭 내부가 아닌 탭 간의 최상단으로는 이동 x
                    navController.popBackStack(it.startDestinationId, inclusive = false)
                }
            }
        }
    }

    private fun initFinishWaitingToast(context: Context) {
        finishWaitingToast =
            SingleMessageToast(
                context,
                context.getString(R.string.message_finish_waiting),
            ).also {
                it.addCallback(
                    object : Toast.Callback() {
                        override fun onToastShown() {
                            backClicked = true
                            super.onToastShown()
                        }

                        override fun onToastHidden() {
                            backClicked = false
                            super.onToastHidden()
                        }
                    },
                )
            }
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?,
    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && onMainFragment) {
            if (backClicked) {
                finishAffinity()
            } else {
                finishWaitingToast.show()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
