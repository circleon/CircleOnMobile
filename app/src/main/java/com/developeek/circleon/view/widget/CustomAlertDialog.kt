package com.developeek.circleon.view.widget

import android.app.Activity
import android.app.AlertDialog

class CustomAlertDialog(
    private val activity: Activity,
    private val message: String,
) {
    private lateinit var dialog: AlertDialog

    init {
        dialog =
            AlertDialog.Builder(activity).also {
                it.setMessage(message)
                    .setPositiveButton(POSITIVE_BUTTON) { dialog, _ ->
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
