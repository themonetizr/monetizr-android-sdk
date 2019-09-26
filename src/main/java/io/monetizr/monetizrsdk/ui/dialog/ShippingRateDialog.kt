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
import io.monetizr.monetizrsdk.ui.adapter.ShippingRateAdapter
import kotlinx.android.synthetic.main.dialog_shipping.*

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

        var adapter = ShippingRateAdapter(ArrayList())
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        listView.setHasFixedSize(true)
        listView.layoutManager = layoutManager
        listView.adapter = adapter

        confirmButtonView.setOnClickListener {
            val item = adapter.getSelectedItem()
            listener!!.onShippingRateSelect(item)
            dialog.dismiss()
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as ShippingRateDialogListener
    }

    companion object {
        const val TAG = "BottomModal"

        fun newInstance(checkout: Checkout): ShippingRateDialog {
            val args = Bundle()
            //args.putParcelable(checkout)
            val fragment = ShippingRateDialog()
            fragment.arguments = args
            return fragment
        }
    }
}

