package com.developeek.circleon.view.listener

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.developeek.circleon.R

class OverflowClickListener : View.OnClickListener {
    private var id: Int = 0
    private lateinit var mContext: Context

    fun onOverflowSelectedListener(
        id: Int,
        context: Context,
    ) {
        this.id = id
        mContext = context
    }

    // TODO: 아마 나중에 menuitem 클릭 리스너를 외부에서 받아야될듯? 뷰모델 관련 작업때문에
    override fun onClick(v: View?) {
        val popupMenu: PopupMenu = object : PopupMenu(mContext, v!!) {}
        popupMenu.inflate(R.menu.menu_post_settings)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.modify_post -> {
                    Toast.makeText(mContext, "수정하기", Toast.LENGTH_SHORT).show()
                }
                R.id.delete_post -> {
                    Toast.makeText(mContext, "삭제하기", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        popupMenu.show()
    }
}
