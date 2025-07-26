package com.developeek.circleon.view.widget

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.developeek.circleon.R

class SingleMessageAlertDialog(
    private val context: Context,
    private val message: String,
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
        hideNegativeButton(view)
    }

    private fun initTitle(view: View) {
        val title = view.findViewById<TextView>(R.id.txtTitle)

        title.text = message
    }

    private fun initPositiveButton(view: View) {
        val btnPositive = view.findViewById<TextView>(R.id.btnPositive)

        btnPositive.text = context.getString(R.string.btn_positive)
        btnPositive.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun hideNegativeButton(view: View) {
        val btnNegative = view.findViewById<TextView>(R.id.btnNegative)

        btnNegative.isVisible = false
    }

    fun show() {
        dialog.show()
    }
}
