package com.themonetizr.monetizrsdk.ui.dialog

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.i18n.addressinput.AddressWidget
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.i18n.addressinput.common.*
import com.themonetizr.monetizrsdk.R
import com.themonetizr.monetizrsdk.dto.ShippingAddress
import kotlinx.android.synthetic.main.dialog_address.*
import org.json.JSONObject


class ShippingAddressDialog : BottomSheetDialogFragment() {
    private val sharedPrefFile = "shipping_address_preference"
    private var listener: ShippingAddressDialogListener? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog = BottomSheetDialog(requireContext(), theme)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences: SharedPreferences = activity!!.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)

        // Set saved values
        val email = sharedPreferences.getString("email","")
        shipping_address_email.setText(email)

        val builder = AddressData.Builder()
        builder.setCountry(sharedPreferences.getString("country",""))
        builder.setRecipient(sharedPreferences.getString("recipient",""))
        builder.setLocality(sharedPreferences.getString("city",""))
        builder.setPostalCode(sharedPreferences.getString("zip",""))
        builder.setAdminArea(sharedPreferences.getString("province", ""))
        val emptyAddressLine: Set<String> = emptySet()
        builder.setAddressLines(sharedPreferences.getStringSet("addresslines", emptyAddressLine)?.toMutableList())
        val address = builder.build()

        val defaultFormOptions = FormOptions()
        defaultFormOptions.setHidden(AddressField.ORGANIZATION)
        val cacheManager = SimpleClientCacheManager()
        val addressWidget = AddressWidget(activity, addresswidget, defaultFormOptions, cacheManager, address)


        // Validate address
        confirm_address.setOnClickListener {
            var hasErrors = false

            // Show input errors from address
            val problems = addressWidget.getAddressProblems()
            val addressData = addressWidget.addressData
            for (problem in problems.problems) {
                addressWidget.displayErrorMessageForField(addressData, problem.key, problem.value)
                hasErrors = true
            }

            if (addressData.recipient == null) {
                addressWidget.displayErrorMessageForField(addressData, AddressField.RECIPIENT, AddressProblemType.MISSING_REQUIRED_FIELD)
                hasErrors = true
            }

            if (shipping_address_email.length() <= 0) {
                shipping_address_email.error = getString(R.string.required_field)
                hasErrors = true
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(shipping_address_email.text).matches() ) {
                shipping_address_email.error = getString( R.string.email_invalid)
                hasErrors = true
            }

            // Dimiss dialog and submit claiming request
            if (!hasErrors) {

                // Save address when first time entered
                val editor:SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("recipient", addressData.recipient)
                editor.putString("city", addressData.locality)
                editor.putString("country", addressData.postalCountry)
                editor.putString("zip", addressData.postalCode)
                editor.putString("province", addressData.administrativeArea)
                editor.putString("email", shipping_address_email.text.toString())
                editor.putStringSet("addresslines", addressData.addressLines.toMutableSet())
                editor.apply()
                editor.commit()

                listener?.onShippingAddressEntered(constructShippingAddress(addressData))
                dialog?.dismiss()
            }
        }
        listener?.onShippingAddresDialogRendered()
    }

    private fun constructShippingAddress(addressData: AddressData): ShippingAddress {
        val addressBody = JSONObject()

        // Recipient
        val recipient = addressData.recipient
        if(recipient.contains(" ")){
            val firstName= recipient.substring(0, recipient.indexOf(" "));
            val lastName = recipient.substring(recipient.indexOf(" "), recipient.length);
            addressBody.put("firstName", firstName)
            addressBody.put("lastName", lastName)
        } else {
            addressBody.put("firstName", addressData.recipient)
            addressBody.put("lastName", addressData.recipient)
        }

        // Straight forward fields
        addressBody.put("city", addressData.locality)
        addressBody.put("country", addressData.postalCountry)
        addressBody.put("zip", addressData.postalCode)
        addressBody.put("province", addressData.administrativeArea)
        addressBody.put("email", shipping_address_email.text)
        addressBody.put("address1", addressData.addressLine1)
        addressBody.put("address2", addressData.addressLine2)

        return ShippingAddress(addressBody)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as ShippingAddressDialogListener
    }


    companion object {
        const val TAG = "ShippingAddressDialog"

        fun newInstance(): ShippingAddressDialog {
            return ShippingAddressDialog()
        }
    }
}