package com.vaultionizer.vaultapp.ui.main.file

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_file_list.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

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
                    /*if(viewModel.onDirectoryChange(it)) {
                        backPressedCallback.isEnabled = true
                    }*/backPressedCallback.isEnabled = true
                    viewModel.onDirectoryChange(it)

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

        val uploadButton = view.findViewById<FloatingActionButton>(R.id.upload)
        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, 1)
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.file_list)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FileFragment.requireContext())
            // addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1
            && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val resolver = requireActivity().applicationContext.contentResolver
                resolver.openInputStream(uri)?.use {
                    BufferedReader(InputStreamReader(it)).use {
                        var line: String? = it.readLine()
                        val builder = StringBuilder()
                        while(line != null) {
                            builder.append(line)
                            line = it.readLine()
                        }

                        Log.e("Vault", uri.toString() + "##" + uri.path + "##" + uri.lastPathSegment ?: "???")
                    }
                }
            }
        }
    }
}