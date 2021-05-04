package com.vaultionizer.vaultapp.ui.main.status

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.util.readableName

class FileStatusListAdapter : RecyclerView.Adapter<FileStatusListAdapter.FileStatusViewHolder>() {

    private var status = emptyList<FileWorkerStatusPair>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileStatusViewHolder {
        TODO("Not yet implemented")
    }

    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: FileStatusViewHolder, position: Int) {
        val state = status[position]

        holder.nameView.text = state.file.name
        holder.stepView.text = state.file.state?.readableName ?: "Status unknown..."
        holder.workerStatusView.text = state.status.readableName
    }

    override fun getItemCount(): Int = status.size

    fun fileStatusChange(status: List<FileWorkerStatusPair>) {
        this.status = status
        notifyDataSetChanged()
    }

    data class FileStatusViewHolder(
        val itemView: View,
        val nameView: TextView = itemView.findViewById(R.id.file_name),
        val stepView: TextView = itemView.findViewById(R.id.file_step),
        val workerStatusView: TextView = itemView.findViewById(R.id.file_worker_status)
    ) : RecyclerView.ViewHolder(itemView)

}