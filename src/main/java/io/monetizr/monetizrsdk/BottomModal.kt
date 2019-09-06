package io.monetizr.monetizrsdk

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONArray
import org.json.JSONObject










interface EditDialogListener {
    fun updateResult(shippingRate: ShippingRate)
}

/**
 * Bottom modal for choosing shipping rates and confirm ordering
 */
class BottomModal : BottomSheetDialogFragment() {

    var listener: EditDialogListener ?= null


    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var bottomModal: View = inflater.inflate(R.layout.modal_bottom, container, false)

        val checkoutInfo = JSONObject(arguments!!.getString("checkoutInfo"))

        val shippingRates: JSONArray = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getJSONObject("availableShippingRates").getJSONArray("shippingRates")

        val subtotalPrice = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getJSONObject("subtotalPriceV2")
        val totalPrice = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getJSONObject("totalPriceV2")
        val totalTax = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getJSONObject("totalTaxV2")

        var subtotalPriceField: TextView = bottomModal.findViewById(R.id.confirm_price_subtotal)
        subtotalPriceField.text = subtotalPriceField.text.toString() + " " + subtotalPrice.getString("amount") + " " + subtotalPrice.getString("currencyCode")

        var priceTaxField: TextView = bottomModal.findViewById(R.id.confirm_price_tax)
        priceTaxField.text = priceTaxField.text.toString() + " " + totalTax.getString("amount") + " " + totalTax.getString("currencyCode")

        var priceTotalWithTaxField: TextView = bottomModal.findViewById(R.id.confirm_price_without_shippping)
        priceTotalWithTaxField.text = priceTotalWithTaxField.text.toString() + " " + totalPrice.getString("amount") + " " + totalPrice.getString("currencyCode")

        var priceTotalWithShipping: TextView = bottomModal.findViewById(R.id.shipping_total_price)

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
        arrayAdapter.holderTotalPrice = priceTotalWithShipping
        arrayAdapter.totalPrice = totalPrice.getString("amount").toDouble()
        arrayAdapter.currencyCode = totalPrice.getString("currencyCode")

        listView.adapter = arrayAdapter

        var submit: View = bottomModal.findViewById(R.id.shipping_submit_button)
        submit.isEnabled = true
        submit.setOnClickListener {

            // Pass back chosen shipping method
            listener!!.updateResult(listItems[arrayAdapter.lastSelectedPosition()])
            dialog.dismiss()
        }

        return bottomModal
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as EditDialogListener
    }

    private inner class ShippingRateAdapter(private val context: Context,
                                            private val dataSource: ArrayList<ShippingRate>) : BaseAdapter(){

        private inner class ShippingItemViewHolder {
            internal var titleTextView: TextView? = null
            internal var subtitleTextView: TextView? = null
            internal var shippingRateView: RadioButton? = null
        }

        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var selected: RadioButton? = null
        var lastSelectedPosition = 0
        var holderTotalPrice: TextView? = null
        var totalPrice: Double = 0.0
        var currencyCode: String? = ""

        override fun getCount(): Int {
            return dataSource.size
        }

        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        fun lastSelectedPosition(): Int{
            return lastSelectedPosition
        }

        fun updateTotalPrice (chosenShippingPrice: Double) {
            holderTotalPrice!!.text = getResources().getString(R.string.confirm_total_with_shipping) + " " + (totalPrice + chosenShippingPrice).toString() + " " + currencyCode
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            var view = convertView
            val viewHolder: ShippingItemViewHolder

            if (view == null) {
                // Get view for row item
                view = inflater.inflate(R.layout.shipping_rate_item, parent, false)

                viewHolder = ShippingItemViewHolder()
                viewHolder.titleTextView = view.findViewById(R.id.shipping_list_title) as TextView
                viewHolder.subtitleTextView = view.findViewById(R.id.shipping_list_detail) as TextView
                viewHolder.shippingRateView = view.findViewById(R.id.shipping_checkbox) as RadioButton

            } else {
                viewHolder = view.tag as ShippingItemViewHolder
            }


            val shippingRate = getItem(position) as ShippingRate
            viewHolder.titleTextView!!.text = shippingRate.title
            viewHolder.subtitleTextView!!.text = shippingRate.price + " " + shippingRate.currencyCode

            // Set first option as selected
            if (position == 0) {
                if (selected == null) {
                    viewHolder.shippingRateView!!.isChecked = true
                    selected = viewHolder.shippingRateView
                    updateTotalPrice(dataSource[position].price!!.toDouble())
                }
            }

            viewHolder.shippingRateView!!.setOnClickListener {
                if (selected != null) {
                    selected!!.isChecked = false
                }
                viewHolder.shippingRateView!!.isChecked = true
                lastSelectedPosition = position
                selected = viewHolder!!.shippingRateView
                updateTotalPrice(dataSource[position].price!!.toDouble())
            }

            view!!.tag = viewHolder

            return view
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