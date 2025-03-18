package com.developeek.circleon.view.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.developeek.circleon.R
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class CircleMemberRoleEditAlertDialog(
    private val context: Context,
    private val glideProvider: GlideProvider,
    private val member: MemberModel,
    private val positiveListenerInitializer: ItemListenerInitializer<Role>,
) {
    private lateinit var alertDialog: AlertDialog
    private lateinit var positiveClickListener: ItemClickListener<Role>

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.dialog_edit_circle_member, null)

        alertDialog =
            AlertDialog.Builder(context, R.style.custom_alert_dialog)
                .setView(view)
                .create()

        initView(view, member)
        initListener(view, member)
        load(view, member)
    }

    private fun initView(
        view: View,
        member: MemberModel,
    ) {
        initRoleSpinner(view, member)
    }

    private fun initRoleSpinner(
        view: View,
        member: MemberModel,
    ) {
        val roleSpinner = view.findViewById<Spinner>(R.id.spCircleRole)
        val spinnerAdapter =
            ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                Role.getCircleRoles().map { it.roleName() },
            )
        spinnerAdapter.setDropDownViewResource(R.layout.item_dropdown)
        roleSpinner.adapter = spinnerAdapter
        roleSpinner.post {
            roleSpinner.dropDownVerticalOffset = roleSpinner.height
        }
    }

    private fun initListener(
        view: View,
        member: MemberModel,
    ) {
        setPositiveListener(view, member)
        setNegativeListener(view)
        setRoleSpinnerListener(view)
    }

    private fun setPositiveListener(
        view: View,
        member: MemberModel,
    ) {
        val btnEditCircleMemberRole = view.findViewById<TextView>(R.id.btnEditCircleMemberRole)

        positiveClickListener =
            object : ItemClickListener<Role> {
                override lateinit var item: Role

                override fun onClick(p0: View?) {
                    positiveListenerInitializer.initialize(item)
                    alertDialog.dismiss()
                }
            }
        btnEditCircleMemberRole.setOnClickListener(positiveClickListener)
    }

    private fun setNegativeListener(view: View) {
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun setRoleSpinnerListener(view: View) {
        val roleSpinner = view.findViewById<Spinner>(R.id.spCircleRole)

        roleSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    positioin: Int,
                    p3: Long,
                ) {
                    positiveClickListener.item = Role.getCircleRoles()[positioin]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    positiveClickListener.item = member.role
                }
            }
    }

    private fun load(
        view: View,
        member: MemberModel,
    ) {
        val memberName = view.findViewById<TextView>(R.id.txtMemberName)
        val memberRole = view.findViewById<Spinner>(R.id.spCircleRole)
        val memberProfileImage = view.findViewById<ImageView>(R.id.imgMemberProfile)

        memberName.text = member.name
        memberRole.setSelection(Role.indexOf(member.role) - 1) // 비회원 제외 관련 설정
        member.profileImgUrl?.let {
            glideProvider.fetchImage(it, context, memberProfileImage)
        } ?: memberProfileImage.setImageResource(R.drawable.ic_user_profile_default)
    }

    fun show() {
        alertDialog.show()
    }
}
