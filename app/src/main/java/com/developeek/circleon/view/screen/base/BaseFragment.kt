package com.developeek.circleon.view.screen.base

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.screen.auth.LoginActivity
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast

open class BaseFragment : Fragment() {
    fun handleEvent(event: Event) {
        when (event) {
            is Event.SendToLoginScreen -> sendUserToLoginScreen(requireActivity())
            is Event.ShowDialog -> showDialog(event.message, requireContext())
            is Event.ShowToast -> showToast(event.message, requireContext())
            is Event.ShowProcessing -> showProcessing()
            is Event.EndProcessing -> endProcessing()
        }
    }

    private fun sendUserToLoginScreen(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        val clearTaskFlags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        intent.setFlags(clearTaskFlags)
        startActivity(intent)
    }

    private fun showToast(
        message: String,
        context: Context,
    ) {
        if (SingleMessageToast.previousFinished()) {
            SingleMessageToast(context, message).show()
        }
    }

    private fun showDialog(
        message: String,
        context: Context,
    ) {
        SingleMessageAlertDialog(context, message).show()
    }

    /**
     * showProcessing, endProcessing
     *
     * 유저의 단순 요청 작업 처리 상태를 나타내는 View 를 통제하기 위한 메서드
     * ScreenFlow 와 별개의 작업에 대한 로딩 인디케이터(또는 그에 준하는 View) 를 컨트롤할 때 사용한다.(ex. 신고하기)
     */
    protected open fun showProcessing() {}

    protected open fun endProcessing() {}
}
