package com.developeek.circleon.view.widget

import android.app.AlertDialog
import android.content.Context

class ContentDeleteAlertDialog(
    private val context: Context,
    private val message: String,
    private val positiveButton: String = POSITIVE_BUTTON,
    private val positiveListener: Runnable,
) {
    private lateinit var dialog: AlertDialog

    init {
        dialog =
            AlertDialog.Builder(context).apply {
                setMessage(message)
                setPositiveButton(positiveButton) { dialog, _ ->
                    positiveListener.run()
                    dialog.dismiss()
                }
                setNegativeButton(NEGATIVE_BUTTON) { dialog, _ ->
                    dialog.dismiss()
                }
            }.create()
    }

    fun show() {
        dialog.show()
    }

    companion object {
        private const val POSITIVE_BUTTON = "삭제"
        private const val NEGATIVE_BUTTON = "취소"
    }
}
