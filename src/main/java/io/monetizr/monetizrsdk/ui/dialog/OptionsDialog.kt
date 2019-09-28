package io.monetizr.monetizrsdk.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.monetizr.monetizrsdk.R
import io.monetizr.monetizrsdk.dto.HierarchyVariant
import io.monetizr.monetizrsdk.dto.Product
import io.monetizr.monetizrsdk.misc.Parameters
import io.monetizr.monetizrsdk.ui.adapter.OptionAdapter
import kotlinx.android.synthetic.main.dialog_options.*
import org.json.JSONObject

class OptionsDialog : DialogFragment() {
    private val selected: ArrayList<HierarchyVariant> = ArrayList()

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
        setStyle(STYLE_NO_TITLE, R.style.DialogShadow)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNoFocusable()

        val json = arguments!!.getString(Parameters.PRODUCT_JSON)
        val product = Product(JSONObject(json))
        val hierarchyList = product.variantHierarchy.toList()

        val adapter = OptionAdapter(::onItemNavigate, ::onLevelNavigate)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        listView.layoutManager = layoutManager
        listView.adapter = adapter

        adapter.goTo(hierarchyList)
        backView.setOnClickListener {
            removeLastSelectItem()
            invalidateSelectTitle()
            if (adapter.goBack() == false) {
                dismiss()
            }
        }
    }

    private fun onLevelNavigate(list: List<HierarchyVariant>) {
        if (list.isEmpty() == false) {
            val first = list[0]
            titleView.text = first.name
        }
    }

    private fun onItemNavigate(item: HierarchyVariant) {
        this.selected.add(item)
        invalidateSelectTitle()

        if (item.childs.isEmpty()) {
            (activity as? OptionsDialogListener)?.onOptionsSelect(selected)
            dismiss()
        }
    }

    private fun removeLastSelectItem() {
        if (this.selected.isEmpty() == false)
            this.selected.removeAt(this.selected.size - 1)
    }

    private fun invalidateSelectTitle() {
        if (selected.isEmpty()) {
            selectedView.visibility = View.GONE

            return
        }
        val builder = StringBuilder()
        builder.append(getString(R.string.selected))

        for (select in selected) {
            builder.append(select.id)
            builder.append(" ")
        }

        selectedView.visibility = View.VISIBLE
        selectedView.text = builder.toString()
    }

    private fun setNoFocusable(){
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }
}