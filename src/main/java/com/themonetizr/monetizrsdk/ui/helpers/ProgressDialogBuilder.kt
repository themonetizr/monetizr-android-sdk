package com.themonetizr.monetizrsdk.ui.helpers

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog

public class ProgressDialogBuilder {

    companion object {
        fun makeProgressDialog(context: Context): AlertDialog? {

            val holderLayout = RelativeLayout(context)
            val params = RelativeLayout.LayoutParams(100, 100)
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            holderLayout.layoutParams = params
            holderLayout.setBackgroundColor(Color.TRANSPARENT)

            val progressBar = ProgressBar(context)
            progressBar.isIndeterminate = true
            progressBar.setBackgroundColor(Color.TRANSPARENT)
            holderLayout.addView(progressBar, params)

            val alertBuilder = AlertDialog.Builder(context)
            alertBuilder.setCancelable(true)
            alertBuilder.setView(holderLayout)

            val progressDialog = alertBuilder.create()
            progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            return progressDialog
        }
    }
}