package com.vaultionizer.vaultapp.ui.main.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.view.IconicsImageView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.FileStatusViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FileStatusFragment : Fragment() {

    private val statusViewModel: FileStatusViewModel by activityViewModels()
    private lateinit var statusAdapter: FileStatusListAdapter

    private lateinit var noTaskText: TextView
    private lateinit var noTaskImage: IconicsImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fileStatusRecyclerView: RecyclerView = view.findViewById(R.id.list_file_status)
        statusAdapter = FileStatusListAdapter()

        fileStatusRecyclerView.layoutManager = LinearLayoutManager(context)
        fileStatusRecyclerView.adapter = statusAdapter

        noTaskText = view.findViewById(R.id.file_status_no_task_text)
        noTaskImage = view.findViewById(R.id.file_status_no_task_image)

        statusViewModel.fileStatus.observe(viewLifecycleOwner) {
            setNoTaskVisibility(it.isEmpty())
            statusAdapter.fileStatusChange(it)
        }
    }

    private fun setNoTaskVisibility(visible: Boolean) {
        val visibility = if (visible)
            View.VISIBLE
        else
            View.INVISIBLE

        noTaskImage.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_smile)
        noTaskText.visibility = visibility
        noTaskImage.visibility = visibility
    }
}