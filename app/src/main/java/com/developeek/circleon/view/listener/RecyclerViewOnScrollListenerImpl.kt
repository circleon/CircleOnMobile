package com.developeek.circleon.view.listener

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewOnScrollListenerImpl : RecyclerView.OnScrollListener() {
    private lateinit var whenScrollEnd: Runnable
    private lateinit var whenScrollUp: Runnable
    private var scrollWorkCompleted = false

    override fun onScrolled(
        recyclerView: RecyclerView,
        dx: Int,
        dy: Int,
    ) {
        super.onScrolled(recyclerView, dx, dy)

        val manager = recyclerView.layoutManager as LinearLayoutManager
        val visibleItemCount = manager.childCount
        val totalItemCount = manager.itemCount
        val firstItem = manager.findFirstVisibleItemPosition()

        if (visibleItemCount + firstItem >= totalItemCount) {
            if (!scrollWorkCompleted) {
                whenScrollEnd.run()
            }
        }
        if (dy < 0) {
            whenScrollUp.run()
        }
    }

    fun setScrollEndListener(whenScrollEnd: Runnable) {
        this.whenScrollEnd = whenScrollEnd
    }

    fun setScrollUpListener(whenScrollUp: Runnable) {
        this.whenScrollUp = whenScrollUp
    }

    fun notifyScrollWorkCompleted() {
        this.scrollWorkCompleted = true
    }

    fun initScrollWorkState() {
        this.scrollWorkCompleted = false
    }
}
