package com.developeek.circleon.view.screen

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ActivityHomeBinding
import com.developeek.circleon.view.screen.calendar.CalendarFragment
import com.developeek.circleon.view.screen.circle.CircleFragment
import com.developeek.circleon.view.screen.directmessage.DirectMessageFragment
import com.developeek.circleon.view.screen.home.HomeFragment
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.screen.mypage.MyPageFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var backClicked = false
    private var onMainFragment = true
    private lateinit var finishWaitingToast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: access 토큰 갱신 결과에 따라 로그인 화면 분기하도록 수정
        val intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)

        initBottomNav()
        initFinishWaitingToast()
    }

    private fun initBottomNav() {
        add(HomeFragment())
        supportFragmentManager.addFragmentOnAttachListener { _, fragment ->
            onMainFragment = fragment is HomeFragment
        }

        binding.btmNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navHome -> replaceTo(HomeFragment())
                R.id.navCalendar -> replaceTo(CalendarFragment())
                R.id.navCircle -> replaceTo(CircleFragment())
                R.id.navDirectMessage -> replaceTo(DirectMessageFragment())
                R.id.navMyPage -> replaceTo(MyPageFragment())
            }

            return@setOnItemSelectedListener true
        }
    }

    private fun add(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(binding.flHome.id, fragment)
        transaction.commit()
    }

    private fun replaceTo(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(binding.flHome.id, fragment)
        transaction.commit()
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
            if (!onMainFragment) {
                replaceTo(HomeFragment())
                binding.btmNav.selectedItemId = R.id.navHome
                return true
            } else {
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
