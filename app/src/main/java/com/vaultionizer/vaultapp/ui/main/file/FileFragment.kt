package com.vaultionizer.vaultapp.ui.main.file

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.arthurivanets.bottomsheets.BottomSheet
import com.arthurivanets.bottomsheets.ktx.showActionPickerBottomSheet
import com.arthurivanets.bottomsheets.sheets.listeners.OnItemSelectedListener
import com.arthurivanets.bottomsheets.sheets.model.Option
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.view.IconicsImageView
import com.nambimobile.widgets.efab.ExpandableFabLayout
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val OPEN_FILE_INTENT_RC = 0

@AndroidEntryPoint
class FileFragment : Fragment(), View.OnClickListener {

    val viewModel: MainActivityViewModel by activityViewModels()

    lateinit var recyclerView: RecyclerView
    lateinit var fileAdapter: FileRecyclerAdapter

    private lateinit var pathRecyclerView: RecyclerView
    private lateinit var pathRecyclerAdapter: PathRecyclerAdapter
    private lateinit var backPressedCallback: OnBackPressedCallback

    private var bottomSheet: BottomSheet? = null
    private var fileStatusDialog: MaterialDialog? = null

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

        val noContentImage = view.findViewById<IconicsImageView>(R.id.iconicsImageView2)
        val noContentText = view.findViewById<TextView>(R.id.text_no_content)

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onDirectoryChange(null)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)

        viewModel.shownElements.observe(viewLifecycleOwner, Observer {
            fileAdapter = FileRecyclerAdapter(
                clickListener = { file ->
                    if (file.isFolder) {
                        viewModel.onDirectoryChange(file)
                        return@FileRecyclerAdapter
                    }

                    if (!file.isBusy) {
                        viewModel.requestDownload(file)
                    }
                },
                optionsClickListener = { file ->
                    showBottomSheetForFile(file)
                }
            ).apply {
                currentElements = it
            }

            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = fileAdapter
            fileAdapter?.notifyDataSetChanged()
            recyclerView.scheduleLayoutAnimation()

            progressBar.visibility = View.GONE

            val visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE
            noContentImage.visibility = visibility
            noContentImage.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_frown)
            noContentText.visibility = visibility
        })

        viewModel.currentDirectory.observe(viewLifecycleOwner) {
            if (it != null) {
                backPressedCallback.isEnabled = it.parent != null
                pathRecyclerAdapter.changeHierarchy(it)
            }
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.file_list)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FileFragment.requireContext())
            // addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            visibility = View.GONE
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireContext(),
                R.anim.layout_animation_fall_down
            )
        }

        pathRecyclerAdapter = PathRecyclerAdapter()
        pathRecyclerView = view.findViewById<RecyclerView>(R.id.path_list)
        pathRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = pathRecyclerAdapter
        }

        val efabLayout = view.findViewById<ExpandableFabLayout>(R.id.file_efab_layout)
        efabLayout.portraitConfiguration.fabOptions.forEach {
            it.setOnClickListener(this)
        }

        viewModel.fileWorkerInfo.observe(viewLifecycleOwner) {
            viewModel.onWorkerInfoChange()
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

        val item = menu.findItem(R.id.action_file_search).actionView as SearchView
        item.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.onSearchQuery(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        item.setOnCloseListener {
            viewModel.onSearchQuery(null)
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_space_delete -> {
                viewModel.requestSpaceDeletion()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OPEN_FILE_INTENT_RC
            && resultCode == Activity.RESULT_OK
        ) {
            data?.data?.also { uri ->
                viewModel.requestUpload(uri)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_option_upload_file -> onClickFileUpload(v)
            R.id.fab_option_upload_folder -> onClickFolderUpload(v)
            R.id.fab_option_create_pc -> onClickCreatePC(v)
        }
    }

    private fun onClickFolderUpload(view: View) {
        val dialog = MaterialDialog(requireContext()).show {
            input { dialog, text ->
                viewModel.requestFolder(text.toString())
            }
            title(R.string.file_viewer_create_folder_title)
            positiveButton(R.string.all_confirm)
        }
        dialog.getInputField().setBackgroundColor(Color.WHITE)
        dialog.show()
    }

    private fun onClickFileUpload(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, OPEN_FILE_INTENT_RC)
    }

    private fun onClickCreatePC(view: View) {
        val action = FileFragmentDirections.actionFileFragmentToCreatePersonalContainerFragment()
        findNavController().navigate(action)
    }

    private fun getActionOptions(): List<Option> {
        return listOf(
            Option().apply {
                id = FileBottomSheetOption.DELETE.id
                iconId = R.drawable.ic_baseline_delete_24
                title = "Delete"
            }
        )
    }

    private fun showBottomSheetForFile(file: VNFile) {
        bottomSheet = showActionPickerBottomSheet(
            options = getActionOptions(),
            onItemSelectedListener = OnItemSelectedListener {
                if (it.id == FileBottomSheetOption.DELETE.id) {
                    showDialog(FileAlertDialogType.DELETE_FILE, positiveClick = { _ ->
                        viewModel.requestDeletion(file)
                    })
                }
                bottomSheet?.dismiss()
            }
        )
    }
}