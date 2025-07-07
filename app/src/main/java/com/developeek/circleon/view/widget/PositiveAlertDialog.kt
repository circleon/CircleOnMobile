package com.developeek.circleon.view.widget

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.developeek.circleon.R

class PositiveAlertDialog(
    private val context: Context,
    private val message: String,
    private val positiveButton: String = context.getString(R.string.btn_positive),
    private val positiveListener: Runnable,
) {
    private lateinit var dialog: AlertDialog

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.dialog_positive_negative, null)

        dialog =
            AlertDialog.Builder(context, R.style.custom_alert_dialog)
                .setView(view)
                .create()

        initView(view)
    }

    private fun initView(view: View) {
        initTitle(view)
        initPositiveButton(view)
        initNegativeButton(view)
    }

    private fun initTitle(view: View) {
        val title = view.findViewById<TextView>(R.id.txtTitle)

        title.text = message
    }

    private fun initPositiveButton(view: View) {
        val btnPositive = view.findViewById<TextView>(R.id.btnPositive)

        btnPositive.text = positiveButton
        btnPositive.setOnClickListener {
            positiveListener.run()
            dialog.dismiss()
        }
    }

    private fun initNegativeButton(view: View) {
        val btnNegative = view.findViewById<TextView>(R.id.btnNegative)

        btnNegative.text = context.getString(R.string.btn_cancel)
        btnNegative.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun show() {
        dialog.show()
    }
}
