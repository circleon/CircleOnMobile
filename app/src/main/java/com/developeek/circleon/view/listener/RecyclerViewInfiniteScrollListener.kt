package com.developeek.circleon.view.listener

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewInfiniteScrollListener : RecyclerView.OnScrollListener() {
    private lateinit var whenScrollEnd: Runnable

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
            whenScrollEnd.run()
        }
    }

    fun setScrollEndListener(whenScrollEnd: Runnable) {
        this.whenScrollEnd = whenScrollEnd
    }
}
