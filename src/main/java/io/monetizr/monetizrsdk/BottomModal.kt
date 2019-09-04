package io.monetizr.monetizrsdk

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONArray
import org.json.JSONObject






interface EditDialogListener {
    fun updateResult(shippingRate: ShippingRate)
}

class BottomModal : BottomSheetDialogFragment() {

    var listener: EditDialogListener ?= null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var bottomModal: View = inflater.inflate(R.layout.modal_bottom, container, false)

        val checkoutInfo: JSONObject = JSONObject(arguments!!.getString("checkoutInfo"))

        val shippingRates: JSONArray = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getJSONObject("availableShippingRates").getJSONArray("shippingRates")


        val checkoutId = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getString("id")
        val subtotalPrice = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getJSONObject("subtotalPriceV2")
        val totalPrice = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getJSONObject("totalPriceV2")
        val totalTax = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getJSONObject("totalTaxV2")


        var subtotalPriceField: TextView = bottomModal.findViewById(R.id.confirm_price_subtotal)
        subtotalPriceField.text = subtotalPriceField.text.toString() + " " + subtotalPrice.getString("amount") + " " + subtotalPrice.getString("currencyCode")

        var priceTaxField: TextView = bottomModal.findViewById(R.id.confirm_price_tax)
        priceTaxField.text = priceTaxField.text.toString() + " " + totalTax.getString("amount") + " " + totalTax.getString("currencyCode")

        var priceTotalWithoutTaxField: TextView = bottomModal.findViewById(R.id.confirm_price_without_shippping)
        priceTotalWithoutTaxField.text = priceTotalWithoutTaxField.text.toString() + " " + totalPrice.getString("amount") + " " + totalPrice.getString("currencyCode")

        // Get form inputs for shipping address
        var listView = bottomModal.findViewById<ListView>(R.id.shipping_list_view)

        val listItems = ArrayList<ShippingRate>()
        for (i in 0 until shippingRates.length()) {
            var item = shippingRates.getJSONObject(i)
            var priceV2 = item.getJSONObject("priceV2")
            var shippingRate = ShippingRate()
            shippingRate.title = item.getString("title")
            shippingRate.handle = item.getString("handle")
            shippingRate.price = priceV2.getString("amount")
            shippingRate.currencyCode = priceV2.getString("currencyCode")
            listItems.add(shippingRate)
        }

        var arrayAdapter = ShippingRateAdapter(activity!!.baseContext, listItems)

        listView.adapter = arrayAdapter

        arrayAdapter?.notifyDataSetChanged()

        listView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            Toast.makeText(
                activity!!.baseContext,
                "Click ListItem Number $position", Toast.LENGTH_LONG
            )
                .show()
        }

        var submit: View = bottomModal.findViewById(R.id.shipping_submit_button)
        submit.isEnabled = true
        submit.setOnClickListener {
            // Pass back chosen shipping method
            listener!!.updateResult(listItems[0])
            dialog.dismiss()
        }

        return bottomModal
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as EditDialogListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Log.i("MonetizrSDK", "why is this modal called again from onview created??" )

        // Submit button


//        var watcher: TextWatcher = object : TextWatcher {
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//            }
//
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//            }
//
//            override fun afterTextChanged(s: Editable) {
////                if( TextUtils.isEmpty(city.text) || TextUtils.isEmpty(country.text)) {
////                    submit.isEnabled = false
////                } else {
////                    submit.isEnabled = true
////                }
//            }
//        }



        // Add text change listeners to required fields
//        firstName.addTextChangedListener(watcher)
//        lastName.addTextChangedListener(watcher)
//        address1.addTextChangedListener(watcher)
//        address2.addTextChangedListener(watcher)
//        city.addTextChangedListener(watcher)
//        country.addTextChangedListener(watcher)
//        zipCode.addTextChangedListener(watcher)
//        phone.addTextChangedListener(watcher)
//        province.addTextChangedListener(watcher)

        // Show keyboard
//        city.requestFocus()
//        dialog.window!!.setSoftInputMode(
//            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
//        )
    }

    private inner class ShippingRateAdapter(private val context: Context,
                                            private val dataSource: ArrayList<ShippingRate>) : BaseAdapter(){

        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return dataSource.size
        }

        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            // Get view for row item
            val rowView = inflater.inflate(R.layout.shipping_rate_item, parent, false)

            // Get title element
            val titleTextView = rowView.findViewById(R.id.shipping_list_title) as TextView

            // Get subtitle element
            val subtitleTextView = rowView.findViewById(R.id.shipping_list_detail) as TextView

            val shippingRate = getItem(position) as ShippingRate

            titleTextView.text = shippingRate.title
            subtitleTextView.text = shippingRate.price + " " + shippingRate.currencyCode

            return rowView
        }

    }

    companion object {
        const val TAG = "BottomModal"
    }
}

class ShippingRate {
    var title: String? = null
    var handle: String? = null
    var price: String? = null
    var currencyCode: String? = null
}