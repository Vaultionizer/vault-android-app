package com.vaultionizer.vaultapp.ui.main.file

import android.content.Context
import android.graphics.Typeface
import android.util.Log
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
import com.vaultionizer.vaultapp.data.model.rest.rf.Element
import com.vaultionizer.vaultapp.data.model.rest.rf.Folder
import com.vaultionizer.vaultapp.data.model.rest.rf.ReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.rf.Type
import com.vaultionizer.vaultapp.data.model.rest.space.SpaceEntry
import java.util.*

class FileRecyclerAdapter(pair: SpaceReferencePair)
    : RecyclerView.Adapter<FileRecyclerAdapter.FileViewHolder>() {

    data class SpaceReferencePair(
        val referenceFile: ReferenceFile,
        val spaceEntry: SpaceEntry
    )

    var dataPair = pair
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val folderHistory = Stack<Folder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_file, parent, false)

        return FileViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val elem = resolveCurrentFiles()[position]

        Log.i("Vault", elem.toString())

        holder.fileImageView.icon = IconicsDrawable(holder.context, chooseElementIcon(elem.name, elem.type))
        holder.fileNameView.text = elem.name

        if(elem.type == Type.FOLDER) {
            holder.fileNameView.setTypeface(null, Typeface.BOLD)
        }
    }

    private fun chooseElementIcon(name: String, type: Type): IIcon = when(type) {
        Type.FOLDER -> chooseFolderIcon(name)
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

    override fun getItemCount(): Int = resolveCurrentFiles().size

    private fun resolveCurrentFiles(): List<Element> {
        if(folderHistory.empty()) {
            return dataPair.referenceFile.elements ?: emptyList()
        }

        return folderHistory.peek().content ?: emptyList()
    }

    data class FileViewHolder(
        val itemView: View,
        val context: Context,
        val fileImageView: IconicsImageView = itemView.findViewById<IconicsImageView>(R.id.file_image),
        val fileNameView: TextView = itemView.findViewById<TextView>(R.id.file_name)
    ) : RecyclerView.ViewHolder(itemView)

}