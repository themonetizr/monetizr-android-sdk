package com.themonetizr.monetizrsdk.ui.dialog

import com.themonetizr.monetizrsdk.dto.ShippingRate
import org.json.JSONObject

interface ShippingRateDialogListener {
    fun onShippingRateSelect(paymentData: String, checkout: JSONObject, shippingRate: ShippingRate)
}