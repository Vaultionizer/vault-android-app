package com.vaultionizer.vaultapp.ui.main.file

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import java.util.*

class PathRecyclerAdapter: RecyclerView.Adapter<PathRecyclerAdapter.PathViewHolder>() {

    private var currentFolderStack = mutableListOf<VNFile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PathViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tree_path_item, parent, false)
        return PathRecyclerAdapter.PathViewHolder(view)
    }

    override fun onBindViewHolder(holder: PathViewHolder, position: Int) {
        val folder = currentFolderStack[currentFolderStack.size - position - 1]
        holder.folderNameText.text = folder.name
    }

    override fun getItemCount(): Int = currentFolderStack.size

    fun changeHierarchy(folder: VNFile) {
        currentFolderStack.clear()
        currentFolderStack.add(folder)

        var current: VNFile? = folder
        while(current?.parent != null) {
            currentFolderStack.add(current.parent!!)
            current = current.parent
        }

        notifyDataSetChanged()
    }

    data class PathViewHolder(
        val view: View,
        val folderNameText: TextView = view.findViewById<TextView>(R.id.folder_name)
    ) : RecyclerView.ViewHolder(view)

}