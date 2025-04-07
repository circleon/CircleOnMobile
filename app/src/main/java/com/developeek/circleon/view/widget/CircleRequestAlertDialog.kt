package com.developeek.circleon.view.widget

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.developeek.circleon.R
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class CircleRequestAlertDialog(
    private val context: Context,
    private val title: String,
    private val content: String = Const.EMPTY_TEXT,
    private val positiveButton: String,
    private val positiveListenerInitializer: ItemListenerInitializer<String>,
) {
    private lateinit var alertDialog: AlertDialog

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.dialog_title_content_negative_positive, null)

        alertDialog = AlertDialog.Builder(context, R.style.custom_alert_dialog).setView(view).create()

        initView(view)
        initListener(view)
    }

    private fun initView(view: View) {
        initTitle(view)
        initPositiveButton(view)
        initContent(view)
    }

    private fun initTitle(view: View) {
        val title = view.findViewById<TextView>(R.id.txtTitle)

        // R.string.title_member_message_dialog
        title.text = this.title
    }

    private fun initPositiveButton(view: View) {
        val positiveButton = view.findViewById<TextView>(R.id.btnLeaveRequest)

        // R.string.btn_leave_request
        positiveButton.text = this.positiveButton
    }

    private fun initContent(view: View) {
        val edtContent = view.findViewById<EditText>(R.id.edtContent)
        val backgroundColor = ContextCompat.getColor(context, R.color.grey_3)

        edtContent.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        edtContent.setText(content)
    }

    private fun initListener(view: View) {
        setPositiveListener(view)
        setNegativeListener(view)
    }

    private fun setPositiveListener(view: View) {
        val btnAcceptLeaveRequest = view.findViewById<TextView>(R.id.btnLeaveRequest)

        val positiveClickListener =
            object : ItemClickListener<String> {
                override lateinit var item: String

                override fun onClick(p0: View?) {
                    item = view.findViewById<EditText>(R.id.edtContent).text.toString()
                    positiveListenerInitializer.initialize(item)
                    alertDialog.dismiss()
                }
            }

        btnAcceptLeaveRequest.setOnClickListener(positiveClickListener)
    }

    private fun setNegativeListener(view: View) {
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun show() {
        alertDialog.show()
    }
}
