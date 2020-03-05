package com.themonetizr.monetizrsdk.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import android.text.util.Linkify
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.themonetizr.monetizrsdk.R
import com.themonetizr.monetizrsdk.misc.Parameters


class MessageDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = arguments!!.getString(Parameters.MESSAGE)!!
        val messageWithUrls = SpannableString(message)
        Linkify.addLinks(messageWithUrls, Linkify.ALL)
        val type = arguments!!.getString(Parameters.MESSAGE_TYPE)!!
        return activity?.let {
            val builder = AlertDialog.Builder(it, com.themonetizr.monetizrsdk.R.style.MessageDialogTheme)
            builder.setMessage(messageWithUrls)
                .setPositiveButton(
                    com.themonetizr.monetizrsdk.R.string.message_dialog_close_btn,
                    DialogInterface.OnClickListener { dialog, id ->
                        // close dialog
                })

            // By default error settings
            builder.setIcon(R.drawable.ic_error)
            builder.setTitle(R.string.something_went_wrong)

            if (type == "success") {
                builder.setTitle(R.string.success_message)
                builder.setIcon(R.drawable.ic_success)
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "MessageDialog"

        fun newInstance(message: String, type: String): MessageDialog {
            val args = Bundle()
            args.putString(Parameters.MESSAGE, message)
            args.putString(Parameters.MESSAGE_TYPE, type)
            val fragment = MessageDialog()
            fragment.arguments = args
            return fragment
        }
    }
}