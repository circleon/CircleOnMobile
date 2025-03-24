package com.developeek.circleon.view.widget

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.developeek.circleon.R
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class EditCommentAlertDialog(
    private val context: Context,
    private val content: String,
    private val positiveListenerInitializer: ItemListenerInitializer<String>,
) {
    private lateinit var alertDialog: AlertDialog

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.dialog_title_content_negative_positive, null)

        alertDialog =
            AlertDialog.Builder(context, R.style.custom_alert_dialog).setView(view).create()

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

        title.text = context.getString(R.string.title_edit_comment_dialog)
    }

    private fun initPositiveButton(view: View) {
        val positiveButton = view.findViewById<TextView>(R.id.btnLeaveRequest)

        positiveButton.text = context.getString(R.string.btn_finish)
    }

    private fun initContent(view: View) {
        val edtContent = view.findViewById<EditText>(R.id.edtContent)
        val backgroundColor = ContextCompat.getColor(context, R.color.grey_3)

        edtContent.setText(content)
        edtContent.backgroundTintList = ColorStateList.valueOf(backgroundColor)
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
