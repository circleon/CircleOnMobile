package com.developeek.circleon.view.listener

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewHideSoftInputListener(
    private val activity: Activity,
) : RecyclerView.OnScrollListener() {
    private val imm: InputMethodManager = activity.getSystemService(InputMethodManager::class.java)

    override fun onScrollStateChanged(
        recyclerView: RecyclerView,
        newState: Int,
    ) {
        super.onScrollStateChanged(recyclerView, newState)
        if (isSoftInputActive(recyclerView)) {
            hideSoftInput(recyclerView)
            removeFocus()
        }
    }

    /**
     * isSoftInputActive(recyclerView: RecyclerView): Boolean
     *
     * hideSoftInputFromWindow() 는 IMM 소프트 키보드 활성화 여부에 따라 boolean 을 리턴한다.
     * 이를 이용하여 키보드 비활성 작업이 중복적으로 발생하는 것을 방지한다.
     */
    private fun isSoftInputActive(recyclerView: RecyclerView) =
        imm.hideSoftInputFromWindow(
            recyclerView.windowToken,
            0,
        )

    private fun hideSoftInput(recyclerView: RecyclerView) {
        imm.hideSoftInputFromWindow(recyclerView.windowToken, 0)
    }

    private fun removeFocus() {
        activity.currentFocus?.clearFocus()
    }
}
