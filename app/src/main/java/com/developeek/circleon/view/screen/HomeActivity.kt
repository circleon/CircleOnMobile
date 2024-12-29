package com.developeek.circleon.view.screen

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ActivityHomeBinding
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

        initView()
    }

    private fun initView() {
        initBottomNav()
        initFinishWaitingToast()
    }

    private fun initBottomNav() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.containerHome) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.btmNav, navController)
        setDestinationChangedListener()
    }

    private fun setDestinationChangedListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            onMainFragment = destination.id == R.id.homeFragment
            if (destination.id == R.id.searchCircleFragment) {
                binding.btmNav.isVisible = false
            } else if (!binding.btmNav.isVisible) {
                binding.btmNav.isVisible = true
            }
        }
    }

    private fun initFinishWaitingToast() {
        finishWaitingToast =
            Toast.makeText(this, FINISH_WAITING_MESSAGE, Toast.LENGTH_SHORT).also {
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (onMainFragment) {
                if (backClicked) {
                    finish()
                } else {
                    finishWaitingToast.show()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val FINISH_WAITING_MESSAGE = "종료를 원하시면 '뒤로'버튼을 한번 더 눌러주세요"
    }
}
