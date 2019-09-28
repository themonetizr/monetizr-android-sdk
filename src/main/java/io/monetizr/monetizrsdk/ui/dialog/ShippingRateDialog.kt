package io.monetizr.monetizrsdk.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.monetizr.monetizrsdk.R
import io.monetizr.monetizrsdk.dto.Checkout
import io.monetizr.monetizrsdk.misc.Parameters
import io.monetizr.monetizrsdk.ui.adapter.ShippingRateAdapter
import kotlinx.android.synthetic.main.dialog_shipping.*
import org.json.JSONObject

class ShippingRateDialog : BottomSheetDialogFragment() {
    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)
    private var listener: ShippingRateDialogListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_shipping, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.isCancelable = false
        val paymentData = arguments!!.getString(Parameters.PAYMENT_DATA)!!
        val checkout = JSONObject(arguments!!.getString(Parameters.CHECKOUT))

        val adapter = initAdapter(checkout)
        confirmButtonView.setOnClickListener {

            val item = adapter.getSelectedItem()
            listener?.onShippingRateSelect(paymentData, checkout, item)
            dialog.dismiss()

        }
    }
    
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as ShippingRateDialogListener
    }

    private fun initAdapter(json: JSONObject): ShippingRateAdapter {
        val checkout = Checkout(json)
        val adapter = ShippingRateAdapter(checkout.shippingRates)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        listView.setHasFixedSize(true)
        listView.layoutManager = layoutManager
        listView.adapter = adapter
        return adapter
    }

    companion object {
        const val TAG = "ShippingRateDialog"

        fun newInstance(paymentData: String, checkout: JSONObject): ShippingRateDialog {
            val args = Bundle()
            args.putString(Parameters.PAYMENT_DATA, paymentData)
            args.putString(Parameters.CHECKOUT, checkout.toString())
            val fragment = ShippingRateDialog()
            fragment.arguments = args
            return fragment
        }
    }
}