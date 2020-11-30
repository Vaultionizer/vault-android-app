package com.vaultionizer.vaultapp.ui.main.file

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FileFragment : Fragment() {

    val viewModel: MainActivityViewModel by activityViewModels()

    lateinit var recyclerView: RecyclerView
    lateinit var fileAdapter: FileRecyclerAdapter

    private lateinit var pathRecyclerView: RecyclerView
    private lateinit var pathRecyclerAdapter: PathRecyclerAdapter

    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        Log.d("Vault", "FileFragment created!")

        val progressBar = view.findViewById<ProgressBar>(R.id.progress_space)
        progressBar.visibility = View.VISIBLE

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onDirectoryChange(null)

                pathRecyclerAdapter.folderList.removeLast()
                pathRecyclerAdapter.notifyDataSetChanged()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)

        viewModel.shownElements.observe(viewLifecycleOwner, Observer {

            fileAdapter = FileRecyclerAdapter(
            ) {
                // TODO: refactor into ViewModel
                if (it.isFolder) {
                    if(!viewModel.onDirectoryChange(it)) {
                        backPressedCallback.isEnabled = true
                    }

                    pathRecyclerAdapter.folderList.add(it)
                    pathRecyclerAdapter.notifyDataSetChanged()
                    Log.d("Vault", "Directory change requested")
                }
            }.apply {
                currentElements = it
            }

            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = fileAdapter
            fileAdapter?.notifyDataSetChanged()
            recyclerView.scheduleLayoutAnimation()

            progressBar.visibility = View.GONE
        })

        /* viewModel.folderHierarchy.observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()) {
                backPressedCallback.isEnabled = false
                fileAdapter?.changeCurrentElements(viewModel.currentReferenceFile.value!!.elements)
            } else {
                fileAdapter?.changeCurrentElements(it.last.content?.toList() ?: emptyList())
            }
            recyclerView.scheduleLayoutAnimation()
            pathRecyclerAdapter.changeHierarchy(it)
        }) */

        recyclerView = view.findViewById<RecyclerView>(R.id.file_list)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FileFragment.requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            visibility = View.GONE
            layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)
        }

        pathRecyclerAdapter = PathRecyclerAdapter()
        pathRecyclerView = view.findViewById<RecyclerView>(R.id.path_list)
        pathRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = pathRecyclerAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().invalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.clear()
        requireActivity().menuInflater.inflate(R.menu.file_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when(item.itemId) {
            R.id.action_space_settings -> {
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

}