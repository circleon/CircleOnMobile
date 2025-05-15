package com.developeek.circleon.view.widget

import android.app.AlertDialog
import android.content.Context

class SingleMessageAlertDialog(
    private val context: Context,
    private val message: String,
) {
    private lateinit var dialog: AlertDialog

    init {
        dialog =
            AlertDialog.Builder(context).apply {
                setMessage(message)
                setPositiveButton(POSITIVE_BUTTON) { dialog, _ ->
                    dialog.dismiss()
                }
            }.create()
    }

    fun show() {
        dialog.show()
    }

    companion object {
        private const val POSITIVE_BUTTON = "확인"
    }
}
