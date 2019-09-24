package io.monetizr.monetizrsdk.ui.dialog

import io.monetizr.monetizrsdk.dto.ShippingRate

interface ShippingRateDialogListener {
    fun onShippingRateSelect(shippingRate: ShippingRate)
}