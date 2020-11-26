package com.vaultionizer.vaultapp.ui.main.file

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.rest.refFile.Folder
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FileFragment : Fragment() {

    val viewModel: MainActivityViewModel by activityViewModels()

    lateinit var recyclerView: RecyclerView
    lateinit var fileAdapter: FileRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Vault", "FileFragment created!")

        val progressBar = view.findViewById<ProgressBar>(R.id.progress_space)
        progressBar.visibility = View.VISIBLE

        viewModel.currentReferenceFile.observe(viewLifecycleOwner, Observer {
            fileAdapter = FileRecyclerAdapter(
                FileRecyclerAdapter.SpaceReferencePair(
                    referenceFile = it,
                    spaceEntry = viewModel.selectedSpace.value!!
                )
            ) {
                // TODO: refactor into ViewModel
                if (it is Folder) {
                    fileAdapter.changeDirectory(it)
                    recyclerView.scheduleLayoutAnimation()
                }
            }

            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = fileAdapter
            fileAdapter.notifyDataSetChanged()
            recyclerView.scheduleLayoutAnimation()

            progressBar.visibility = View.GONE
        })

        recyclerView = view.findViewById<RecyclerView>(R.id.file_list)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FileFragment.requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            visibility = View.GONE
            layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            fileAdapter.previousDirectory()
        }
    }


}