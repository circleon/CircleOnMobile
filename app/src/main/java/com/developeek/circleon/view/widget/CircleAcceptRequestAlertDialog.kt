package com.developeek.circleon.view.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.developeek.circleon.R
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class CircleAcceptRequestAlertDialog(
    private val context: Context,
    private val title: String,
    private val member: MemberModel,
    private val positiveListenerInitializer: ItemListenerInitializer<MemberModel>,
) {
    private lateinit var alertDialog: AlertDialog

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.dialog_title_content_negative_positive, null)

        alertDialog =
            AlertDialog.Builder(context, R.style.custom_alert_dialog)
                .setView(view)
                .create()

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

        // title_member_message_dialog_for_accept
        title.text = String.format(this.title, member.name)
    }

    private fun initPositiveButton(view: View) {
        val positiveButton = view.findViewById<TextView>(R.id.btnLeaveRequest)

        positiveButton.text = context.getString(R.string.btn_accept_leave_request)
    }

    private fun initContent(view: View) {
        val edtContent = view.findViewById<EditText>(R.id.edtContent)

        edtContent.setText(member.message)
        edtContent.isClickable = false
        edtContent.isFocusable = false
        edtContent.isCursorVisible = false
        edtContent.background = ContextCompat.getDrawable(context, R.drawable.bg_card)
    }

    private fun initListener(view: View) {
        setPositiveListener(view)
        setNegativeListener(view)
    }

    private fun setPositiveListener(view: View) {
        val btnAcceptRequest = view.findViewById<TextView>(R.id.btnLeaveRequest)

        val positiveClickListener =
            object : ItemClickListener<MemberModel> {
                override lateinit var item: MemberModel

                override fun onClick(p0: View?) {
                    positiveListenerInitializer.initialize(item)
                    alertDialog.dismiss()
                }
            }.apply {
                item = member
            }

        btnAcceptRequest.setOnClickListener(positiveClickListener)
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
