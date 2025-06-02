package com.developeek.circleon.view.widget

import android.app.AlertDialog
import android.content.Context
import com.developeek.circleon.R

class PositiveAlertDialog(
    private val context: Context,
    private val message: String,
    private val positiveButton: String = context.getString(R.string.btn_positive),
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
                setNegativeButton(context.getString(R.string.btn_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
            }.create()
    }

    fun show() {
        dialog.show()
    }
}
