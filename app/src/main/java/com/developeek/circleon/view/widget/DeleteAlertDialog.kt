package com.developeek.circleon.view.widget

import android.app.Activity
import android.app.AlertDialog

class DeleteAlertDialog(
    private val activity: Activity,
    private val message: String,
    private val positiveListener: Runnable,
) {
    private lateinit var dialog: AlertDialog

    init {
        dialog =
            AlertDialog.Builder(activity).apply {
                setMessage(message)
                setPositiveButton(POSITIVE_BUTTON) { dialog, _ ->
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
