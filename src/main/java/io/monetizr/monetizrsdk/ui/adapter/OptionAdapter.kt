package io.monetizr.monetizrsdk.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.monetizr.monetizrsdk.R
import io.monetizr.monetizrsdk.dto.Option
import io.monetizr.monetizrsdk.dto.Variant

class OptionAdapter(private val items: ArrayList<String>) : RecyclerView.Adapter<OptionAdapter.ViewHolder>() {
    var selected: Variant? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val photoView = inflater.inflate(R.layout.item_option, parent, false)
        return ViewHolder(photoView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nameView.text = item.title
        holder.priceView.text = item.priceV2.formatString()
        holder.current = item

        if (selected == item) {
            holder.checkView.visibility = View.VISIBLE
        } else {
            holder.checkView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameView: TextView = itemView.findViewById(R.id.nameView)
        var priceView: TextView = itemView.findViewById(R.id.priceView)
        var checkView: View = itemView.findViewById(R.id.checkView)
        var current: Variant? = null

        init {
            itemView.setOnClickListener {
                selected = current
                notifyDataSetChanged()
            }
        }
    }
}
