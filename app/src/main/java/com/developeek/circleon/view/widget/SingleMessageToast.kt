package com.developeek.circleon.view.widget

import android.content.Context
import android.widget.Toast
import com.developeek.circleon.domain.utils.Const

class SingleMessageToast(
    private val context: Context,
    private val message: CharSequence,
) : Toast(context) {
    init {
        super.setText(message)
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
