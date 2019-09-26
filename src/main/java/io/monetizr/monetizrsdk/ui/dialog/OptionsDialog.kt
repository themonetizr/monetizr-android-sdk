package io.monetizr.monetizrsdk.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.monetizr.monetizrsdk.R
import io.monetizr.monetizrsdk.dto.Product
import io.monetizr.monetizrsdk.misc.Parameters
import io.monetizr.monetizrsdk.ui.adapter.OptionAdapter
import kotlinx.android.synthetic.main.dialog_options.*
import org.json.JSONObject

class OptionsDialog : DialogFragment() {

    companion object {
        fun newInstance(product: String): OptionsDialog {
            val args = Bundle()
            val fragment = OptionsDialog()
            args.putString(Parameters.PRODUCT_JSON, product)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogNoShadow)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val json = arguments!!.getString(Parameters.PRODUCT_JSON)
        val product = Product(JSONObject(json))

        val adapter = OptionAdapter(product.variants)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        listView.setHasFixedSize(true)
        listView.layoutManager = layoutManager
        listView.adapter = adapter
    }
}