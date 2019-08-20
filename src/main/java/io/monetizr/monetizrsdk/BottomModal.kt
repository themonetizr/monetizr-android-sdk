package io.monetizr.monetizrsdk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment



class BottomModal : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.modal_bottom, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get form inputs for shipping address
        var firstName: View = view.findViewById(R.id.shipping_first_name)
        var lastName: View = view.findViewById(R.id.shipping_last_name)
        var address1: View = view.findViewById(R.id.shipping_address1)
        var address2: View = view.findViewById(R.id.shipping_address2)
        var city: View = view.findViewById(R.id.shipping_city)
        var country: View = view.findViewById(R.id.shipping_country)
        var zipCode: View = view.findViewById(R.id.shipping_zip)
        var phone: View = view.findViewById(R.id.shipping_phone)

        // Show keyboard
        firstName.requestFocus()
        dialog.window!!.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        var submit: View = view.findViewById(R.id.shipping_submit_button)

        submit.setOnClickListener { dialog.dismiss() }
    }

    companion object {
        const val TAG = "BottomModal"
    }
}