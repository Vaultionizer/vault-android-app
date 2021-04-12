package com.vaultionizer.vaultapp.ui.main.pc

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.pc.PCFile


class ViewPCRecyclerViewAdapter(
    private val file: PCFile
) : RecyclerView.Adapter<ViewPCRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pc_category_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.idView.text = file.categories[position].name
        holder.contentView.text = file.categories[position].name
    }

    override fun getItemCount(): Int = file.categories.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.item_number)
        val contentView: TextView = view.findViewById(R.id.content)

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}