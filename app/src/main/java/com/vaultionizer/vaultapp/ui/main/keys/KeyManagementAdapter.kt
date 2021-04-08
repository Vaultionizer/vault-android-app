package com.vaultionizer.vaultapp.ui.main.keys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.util.Constants

class KeyManagementAdapter(private val clickListener: (VNSpace) -> Unit) :
    RecyclerView.Adapter<KeyManagementAdapter.ViewHolder>() {

    private var dataSet = mutableListOf<VNSpace>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val deleteButton: ImageButton

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.textView5)
            deleteButton = view.findViewById(R.id.key_delete_button)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fragment_key, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text =
            "Keyname: ${Constants.VN_KEY_PREFIX}${dataSet[position].name ?: dataSet[position].id}"
        viewHolder.deleteButton.setOnClickListener {
            clickListener(dataSet[position])
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun updateSpaces(spaceList: MutableList<VNSpace>) {
        this.dataSet = spaceList
        notifyDataSetChanged()
    }
}