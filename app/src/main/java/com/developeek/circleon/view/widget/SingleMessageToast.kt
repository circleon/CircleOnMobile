package com.developeek.circleon.view.widget

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.developeek.circleon.R
import com.developeek.circleon.domain.utils.Const

class SingleMessageToast(
    private val context: Context,
    private val message: CharSequence,
) : Toast(context) {
    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.toast_single_message, null)

        view.findViewById<TextView>(R.id.txtMessage).text = message
        super.setView(view)
    }

    override fun show() {
        if (message != Const.EMPTY_TEXT) {
            toastedTime = System.currentTimeMillis() / MILLIS_DIVIDER
            super.show()
        }
    }

    companion object {
        fun previousFinished() = (System.currentTimeMillis() / MILLIS_DIVIDER) - toastedTime > TOAST_SHORT

        private const val TOAST_SHORT = 2
        private const val MILLIS_DIVIDER = 1000
        private var toastedTime: Long = 0
    }
}
