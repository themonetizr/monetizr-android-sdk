package com.themonetizr.monetizrsdk.ui.helpers

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.themonetizr.monetizrsdk.R

class ErrorMessageBuilder {

    companion object {
        fun makeDialog(context: Context, message: String): AlertDialog? {

            val alertBuilder = AlertDialog.Builder(context, R.style.MessageDialogTheme)
            alertBuilder.setCancelable(true)
            alertBuilder.setIcon(R.drawable.ic_error)
            alertBuilder.setTitle(R.string.something_went_wrong)
            alertBuilder.setMessage(message)

            val errorDialog = alertBuilder.create()

            return errorDialog
        }
    }
}