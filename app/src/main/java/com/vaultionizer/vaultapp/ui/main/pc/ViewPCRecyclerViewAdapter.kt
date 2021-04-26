package com.vaultionizer.vaultapp.ui.main.pc

import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.pc.PCCategory
import com.vaultionizer.vaultapp.data.pc.PCFile
import com.vaultionizer.vaultapp.data.pc.PCPair
import kotlinx.android.synthetic.main.sample_pair_view.*
import kotlinx.android.synthetic.main.sample_pair_view.view.*


class ViewPCRecyclerViewAdapter(
    private val file: PCFile,
    private val fragment: ViewPCItemClickListener,
    private val openedCategoryId: Int? = null
) : RecyclerView.Adapter<ViewPCRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pc_category_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var categoryId: Int?
        if (position == 0) {
            holder.contentView.text = "<Uncategorized>"
            setOuterLayoutListeners(holder, null)
            categoryId = null
        } else {
            val category = file.categories[position - 1]
            holder.contentView.text = category.name
            setOuterLayoutListeners(holder, category)
            categoryId = category.id
        }

        val inflater = LayoutInflater.from(holder.context)

        for (pair in file.pairs) {
            if (pair.categoryId != categoryId) continue
            initPairLayout(holder, inflater, pair)
        }

        if (openedCategoryId != categoryId) {
            holder.pairLayout.visibility = GONE
        } else {
            holder.pairLayout.visibility = VISIBLE
        }
    }

    override fun getItemCount(): Int = file.categories.size + 1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val outerLayout: ConstraintLayout = view.findViewById(R.id.outerLayout)
        val itemLayout: ConstraintLayout = view.findViewById(R.id.item_layout)
        val contentView: TextView = view.findViewById(R.id.content)
        val pairLayout: LinearLayout = view.findViewById(R.id.pair_container)
        val context = view.context

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    private fun setOuterLayoutListeners(holder: ViewHolder, category: PCCategory?) {
        holder.outerLayout.setOnClickListener {
            holder.pairLayout.visibility = if (holder.pairLayout.isVisible) GONE else VISIBLE
        }
        if (category == null) return
        holder.outerLayout.setOnLongClickListener {
            fragment.openCategoryOptions(category)
            return@setOnLongClickListener true
        }
    }

    private fun initPairLayout(holder: ViewHolder, inflater: LayoutInflater, pair: PCPair) {
        val frag = inflater.inflate(R.layout.sample_pair_view, null, false)
        val layout = frag.pairView.keyLayout
        frag.setOnLongClickListener {
            fragment.openPairOptions(pair)
            return@setOnLongClickListener true
        }

        layout.pairKey.text = pair.key
        layout.pairValue.text = pair.value
        holder.pairLayout.addView(frag)
    }


}