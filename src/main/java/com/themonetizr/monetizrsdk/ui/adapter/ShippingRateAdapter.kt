package com.themonetizr.monetizrsdk.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.themonetizr.monetizrsdk.R
import com.themonetizr.monetizrsdk.dto.ShippingRate

class ShippingRateAdapter(private val items: ArrayList<ShippingRate>) : RecyclerView.Adapter<ShippingRateAdapter.ViewHolder>() {
    private var current: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val photoView = inflater.inflate(R.layout.item_shipping_rate, parent, false)
        return ViewHolder(photoView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.checkView.isChecked = current == position
        holder.titleView.text = item.title
        holder.textView.text = item.price[0].formatString()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getSelectedItem(): ShippingRate {
        return items[current]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var checkView: CheckBox = itemView.findViewById(R.id.checkboxView)
        var titleView: TextView = itemView.findViewById(R.id.titleView)
        var textView: TextView = itemView.findViewById(R.id.textView)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                current = position
                notifyDataSetChanged()
            }
        }
    }
}
