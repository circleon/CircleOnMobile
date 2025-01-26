package com.developeek.circleon.view.widget

import android.app.Activity
import android.app.AlertDialog
import android.view.ViewGroup
import android.widget.EditText
import com.developeek.circleon.view.listener.ItemListenerInitializer

class TextInputAlertDialog(
    private val activity: Activity,
    private val input: String? = null,
    private val positiveListenerInitializer: ItemListenerInitializer<String>,
) {
    private lateinit var dialog: AlertDialog

    init {
        val editText =
            EditText(activity).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                setText(input)
            }
        dialog =
            AlertDialog.Builder(activity).apply {
                setView(editText)
                setPositiveButton(POSITIVE_BUTTON) { dialog, _ ->
                    positiveListenerInitializer.initialize(editText.text.toString())
                    dialog.dismiss()
                }
                setNegativeButton(NEGATIVE_BUTTON) { dialog, _ ->
                    dialog.dismiss()
                }
            }.create()
    }

    fun show() {
        dialog.show()
    }

    companion object {
        private const val POSITIVE_BUTTON = "확인"
        private const val NEGATIVE_BUTTON = "취소"
    }
}
