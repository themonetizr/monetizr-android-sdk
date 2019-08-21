package io.monetizr.monetizrsdk

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONObject


interface EditDialogListener {
    fun updateResult(shippingAddress: JSONObject)
}

class BottomModal : BottomSheetDialogFragment() {

    var listener: EditDialogListener ?= null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.modal_bottom, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i("MonetizrSDK", "why is this modal called again from onattach??")
        listener = context as EditDialogListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("MonetizrSDK", "why is this modal called again from onview created??")

        // Get form inputs for shipping address
        var firstName: EditText = view.findViewById(R.id.shipping_first_name)
        var lastName: EditText = view.findViewById(R.id.shipping_last_name)
        var address1: EditText = view.findViewById(R.id.shipping_address1)
        var address2: EditText = view.findViewById(R.id.shipping_address2)
        var city: EditText = view.findViewById(R.id.shipping_city)
        var country: EditText = view.findViewById(R.id.shipping_country)
        var zipCode: EditText = view.findViewById(R.id.shipping_zip)
        var phone: EditText = view.findViewById(R.id.shipping_phone)
        var province: EditText = view.findViewById(R.id.shipping_province)

        // Submit button
        var submit: View = view.findViewById(R.id.shipping_submit_button)
        submit.isEnabled = false
        submit.setOnClickListener {
            val jsonBody = JSONObject()
            jsonBody.put("firstName", firstName.text)
            jsonBody.put("lastName", lastName.text)
            jsonBody.put("address1", address1.text)
            jsonBody.put("address2", address2.text)
            jsonBody.put("city", city.text)
            jsonBody.put("province", province.text)
            jsonBody.put("country", country.text)
            jsonBody.put("zip", zipCode.text)
            jsonBody.put("phone", phone.text)

            listener!!.updateResult(jsonBody)
            dialog.dismiss()
        }

        var watcher: TextWatcher = object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if(TextUtils.isEmpty(firstName.text) || TextUtils.isEmpty(lastName.text) || TextUtils.isEmpty(address1.text)
                    || TextUtils.isEmpty(city.text) || TextUtils.isEmpty(country.text) || TextUtils.isEmpty(zipCode.text) || TextUtils.isEmpty(province.text)) {
                    submit.isEnabled = false
                } else {
                    submit.isEnabled = true
                }
            }
        }

        // Add text change listeners to required fields
        firstName.addTextChangedListener(watcher)
        lastName.addTextChangedListener(watcher)
        address1.addTextChangedListener(watcher)
        address2.addTextChangedListener(watcher)
        city.addTextChangedListener(watcher)
        country.addTextChangedListener(watcher)
        zipCode.addTextChangedListener(watcher)
        phone.addTextChangedListener(watcher)
        province.addTextChangedListener(watcher)

        // Show keyboard
        firstName.requestFocus()
        dialog.window!!.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
    }

    companion object {
        const val TAG = "BottomModal"


    }
}