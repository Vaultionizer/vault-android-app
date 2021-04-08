package com.vaultionizer.vaultapp.ui.main.file

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.view.IconicsImageView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import java.text.DateFormat

class FileRecyclerAdapter(
    private val clickListener: (VNFile) -> Unit,
    private val optionsClickListener: (VNFile) -> Unit
) : RecyclerView.Adapter<FileRecyclerAdapter.FileViewHolder>() {

    var currentElements = listOf<VNFile>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_file, parent, false)
        return FileViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val elem = currentElements[position]

        holder.fileImageView.icon =
            IconicsDrawable(holder.context, chooseElementIcon(elem.name, elem.isFolder))
        holder.fileDownloaded.visibility =
            if (elem.isDownloaded(holder.view.context) || elem.state == VNFile.State.AVAILABLE_OFFLINE) {
                View.VISIBLE
                Log.e("Vault", "Visible")
            } else {
                Log.e("Vault", "Invisible")
                View.INVISIBLE
            }

        Log.e("Vault", "POS ${position} SIZE ${currentElements.size}")

        holder.fileNameView.text = "${elem.name}"
        holder.fileDate.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(elem.lastUpdated ?: elem.createdAt!!)

        if (elem.isFolder) {
            holder.fileNameView.setTypeface(null, Typeface.BOLD)
        } else {
            holder.fileNameView.typeface = null
        }

        holder.itemView.setOnClickListener {
            clickListener(elem)
        }
        holder.fileMore.setOnClickListener {
            optionsClickListener(elem)
        }
    }

    override fun getItemCount(): Int = currentElements.size

    private fun chooseElementIcon(name: String, isFolder: Boolean): IIcon = if (isFolder) {
        chooseFolderIcon(name)
    } else chooseFileIcon(name)

    private fun chooseFileIcon(name: String): IIcon {
        val type = name.split(".").run {
            get(size - 1).toLowerCase()
        }

        return when (type) {
            "pdf" -> FontAwesome.Icon.faw_file_pdf
            "jpg", "png", "jpeg", "gif", "svg" -> FontAwesome.Icon.faw_image
            else -> FontAwesome.Icon.faw_file
        }
    }

    private fun chooseFolderIcon(name: String): IIcon {
        return FontAwesome.Icon.faw_folder_open
    }

    data class FileViewHolder(
        val view: View,
        val context: Context,
        val fileImageView: IconicsImageView = view.findViewById<IconicsImageView>(R.id.file_image),
        val fileNameView: TextView = view.findViewById<TextView>(R.id.file_name),
        val fileDownloaded: IconicsImageView = view.findViewById<IconicsImageView>(R.id.file_downloaded),
        val fileDate: TextView = view.findViewById(R.id.file_date),
        val fileMore: ImageButton = view.findViewById(R.id.file_more)
    ) : RecyclerView.ViewHolder(view)

}