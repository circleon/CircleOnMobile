package com.developeek.circleon.view.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.developeek.circleon.R
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class CircleMemberLeaveRequestAcceptAlertDialog(
    private val context: Context,
    private val member: MemberModel,
    private val positiveListenerInitializer: ItemListenerInitializer<MemberModel>,
) {
    private lateinit var alertDialog: AlertDialog

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.dialog_accept_circle_member_leave_request, null)

        alertDialog =
            AlertDialog.Builder(context, R.style.custom_alert_dialog)
                .setView(view)
                .create()

        initView(view)
        initListener(view)
    }

    private fun initView(view: View) {
        val title = view.findViewById<TextView>(R.id.txtTitle)

        title.text = String.format(context.getString(R.string.title_member_message_dialog), member.name)
    }

    private fun initListener(view: View) {
        setPositiveListener(view)
        setNegativeListener(view)
    }

    private fun setPositiveListener(view: View) {
        val btnAcceptLeaveRequest = view.findViewById<TextView>(R.id.btnAcceptLeaveRequest)

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
