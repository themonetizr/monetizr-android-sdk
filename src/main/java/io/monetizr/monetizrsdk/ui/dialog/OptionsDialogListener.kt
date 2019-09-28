package io.monetizr.monetizrsdk.ui.dialog

import io.monetizr.monetizrsdk.dto.HierarchyVariant

interface OptionsDialogListener {
    fun onOptionsSelect(options: ArrayList<HierarchyVariant>)
}