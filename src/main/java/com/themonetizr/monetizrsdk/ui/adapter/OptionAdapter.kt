package com.themonetizr.monetizrsdk.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.themonetizr.monetizrsdk.R
import com.themonetizr.monetizrsdk.dto.HierarchyVariant

class OptionAdapter(val onItemNavigate: (HierarchyVariant) -> Any, val onLevelNavigate: (List<HierarchyVariant>) -> Any) : RecyclerView.Adapter<OptionAdapter.ViewHolder>() {
    private val items: ArrayList<HierarchyVariant> = ArrayList()
    private var maxOptionsLevel: Int = 0

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

        // Hiding next icon in the last selector element
        if (item.level == this.maxOptionsLevel - 1 || this.maxOptionsLevel == 1 || this.maxOptionsLevel == 0) {
            holder.optionNextIcon.visibility = View.INVISIBLE
        } else {
            holder.optionNextIcon.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setMaxOptionsLevel(maxLevel: Int) {
        this.maxOptionsLevel = maxLevel
    }

    fun goBack(): Boolean {
        if (items.isEmpty() == false) {
            val first = items[0]
            val level = first.level
            if (level > 0 && first.parents != null && first.parents.isEmpty() == false) {
                goTo(first.parents.toList())
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

    fun goTo(param: List<HierarchyVariant>) {
        if (param.isEmpty()) return
        this.items.clear()
        this.items.addAll(param)
        this.onLevelNavigate(param)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameView: TextView = itemView.findViewById(R.id.nameView)
        var priceView: TextView = itemView.findViewById(R.id.priceView)
        var optionNextIcon: ImageView = itemView.findViewById(R.id.optionNextIcon)
        var current: HierarchyVariant? = null

        init {
            itemView.setOnClickListener {
                current?.let {
                    if (it.childs.toList().isEmpty() == false) {
                        goTo(it.childs.toList())
                        onItemNavigate(it)
                    } else {
                        onItemNavigate(it)
                    }
                }
            }
        }
    }
}
