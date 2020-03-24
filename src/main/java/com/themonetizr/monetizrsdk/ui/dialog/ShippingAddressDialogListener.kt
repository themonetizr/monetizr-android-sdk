package com.themonetizr.monetizrsdk.ui.dialog

import com.themonetizr.monetizrsdk.dto.ShippingAddress

interface ShippingAddressDialogListener {
    fun onShippingAddressEntered(address: ShippingAddress)
    fun onShippingAddresDialogRendered()
}