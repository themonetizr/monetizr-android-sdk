package com.themonetizr.monetizrsdk.ui.dialog

import com.themonetizr.monetizrsdk.dto.HierarchyVariant

interface OptionsDialogListener {
    fun onOptionsSelect(options: ArrayList<HierarchyVariant>)
}