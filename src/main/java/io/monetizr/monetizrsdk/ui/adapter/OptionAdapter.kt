package io.monetizr.monetizrsdk.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.monetizr.monetizrsdk.R
import io.monetizr.monetizrsdk.dto.HierarchyVariant
import io.monetizr.monetizrsdk.dto.Option
import io.monetizr.monetizrsdk.dto.Variant

class OptionAdapter(private val items: List<HierarchyVariant>, val itemTap: (HierarchyVariant) -> Any) : RecyclerView.Adapter<OptionAdapter.ViewHolder>() {
    var selected: HierarchyVariant? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val photoView = inflater.inflate(R.layout.item_option, parent, false)
        return ViewHolder(photoView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nameView.text = item.id
        holder.priceView.text = item.price.formatString()
        holder.current = item
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameView: TextView = itemView.findViewById(R.id.nameView)
        var priceView: TextView = itemView.findViewById(R.id.priceView)
        var current: HierarchyVariant? = null

        init {
            itemView.setOnClickListener {
                selected = current
                if (selected != null) {
                    itemTap(selected!!)
                }
            }
        }
    }
}
