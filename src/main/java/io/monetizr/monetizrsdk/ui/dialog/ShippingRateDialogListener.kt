package io.monetizr.monetizrsdk.ui.dialog

import io.monetizr.monetizrsdk.dto.ShippingRate
import org.json.JSONObject

interface ShippingRateDialogListener {
    fun onShippingRateSelect(paymentData: String, checkout: JSONObject, shippingRate: ShippingRate)
}