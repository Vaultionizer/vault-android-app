package com.vaultionizer.vaultapp.ui.main.file

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.view.IconicsImageView
import com.vaultionizer.vaultapp.R

class FileRecyclerAdapter : RecyclerView.Adapter<FileRecyclerAdapter.FileViewHolder>() {

    data class File(val type: Int, val name: String)
    private val data = ArrayList<File>().apply {
        add(File(0, "Photos"))
        add(File(0, "Homework ;)"))
        add(File(1, "julien-unitymedia-meme.jpg"))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_file, parent, false)

        return FileViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val elem = data[position]

        holder.fileImageView.icon = IconicsDrawable(holder.context, chooseElementIcon(elem.name, elem.type))
        holder.fileNameView.text = elem.name

        if(elem.type == 0) {
            holder.fileNameView.setTypeface(null, Typeface.BOLD)
        }
    }

    private fun chooseElementIcon(name: String, type: Int): IIcon = when(type) {
        0 -> chooseFolderIcon(name)
        else -> chooseFileIcon(name)
    }

    private fun chooseFileIcon(name: String): IIcon {
        val type = name.split(".").run {
            get(size - 1).toLowerCase()
        }

        return when(type) {
            "pdf" -> FontAwesome.Icon.faw_file_pdf
            "jpg", "png", "jpeg", "gif", "svg" -> FontAwesome.Icon.faw_image
            else -> FontAwesome.Icon.faw_file
        }
    }

    private fun chooseFolderIcon(name: String): IIcon {
        return FontAwesome.Icon.faw_folder_open
    }

    override fun getItemCount(): Int = data.size

    data class FileViewHolder(
        val itemView: View,
        val context: Context,
        val fileImageView: IconicsImageView = itemView.findViewById<IconicsImageView>(R.id.file_image),
        val fileNameView: TextView = itemView.findViewById<TextView>(R.id.file_name)
    ) : RecyclerView.ViewHolder(itemView)

}