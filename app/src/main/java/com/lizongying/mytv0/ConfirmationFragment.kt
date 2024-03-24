package com.lizongying.mytv0

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ConfirmationFragment(private val listener: ConfirmationListener) :
    DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("确认")
                .setMessage("确认更新吗？")
                .setPositiveButton(
                    "确定"
                ) { _, _ ->
                    listener.onConfirm()
                }
                .setNegativeButton(
                    "取消"
                ) { _, _ ->
                    listener.onCancel()
                }
            // 创建并返回 AlertDialog 对象
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface ConfirmationListener {
        fun onConfirm()
        fun onCancel()
    }
}

