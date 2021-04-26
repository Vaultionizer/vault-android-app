package com.vaultionizer.vaultapp.ui.main.pc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arthurivanets.bottomsheets.ktx.showActionPickerBottomSheet
import com.arthurivanets.bottomsheets.sheets.ActionPickerBottomSheet
import com.arthurivanets.bottomsheets.sheets.listeners.OnItemSelectedListener
import com.arthurivanets.bottomsheets.sheets.model.Option
import com.nambimobile.widgets.efab.ExpandableFabLayout
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.pc.PCCategory
import com.vaultionizer.vaultapp.data.pc.PCPair
import com.vaultionizer.vaultapp.ui.main.file.FileAlertDialogType
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import com.vaultionizer.vaultapp.ui.viewmodel.PCViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_pc_category_list.view.*

enum class PairOptions(val id: Long) {
    DELETE(0),
    EDIT(1),
    COPY_VALUE(2)
}

enum class CategoryOptions(val id: Long) {
    EDIT(0),
    DELETE_ONLY_CAT(1),
    DELETE_CAT_AND_PAIRS(2)
}

@AndroidEntryPoint
class ViewPCFragment : Fragment(), ViewPCInterface {

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            ViewPCFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    private var columnCount = 1
    private var dialog: SweetAlertDialog? = null
    private var bottomSheet: ActionPickerBottomSheet? = null
    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    private val viewModel: PCViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.view_pc_category_list, container, false)

        // Set the adapter
        val frag = this as ViewPCInterface
        if (view.view_pc_category_list is RecyclerView) {
            with(view.view_pc_category_list) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = ViewPCRecyclerViewAdapter(viewModel.pcRepository.getCurrentFile(), frag)
            }
        }
        requireActivity()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fabLayout = view.findViewById<ExpandableFabLayout>(R.id.fab_view_pc_layout)
        fabLayout.portraitConfiguration.fabOptions.forEach {
            when (it.id) {
                R.id.fab_view_pc_create_category ->
                    it.setOnClickListener { transitionToEditCategory() }

                R.id.fab_view_pc_create_pair ->
                    it.setOnClickListener { transitionToEditPair() }
            }
        }
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleNavigateBack()
            }
        }
        setBackPressedHandler()
        refreshRecyclerView()
    }

    private fun setBackPressedHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    private fun resetBackPressedHandler() {
        backPressedCallback.isEnabled = false
        backPressedCallback.remove()
    }

    private fun handleNavigateBack() {
        if (viewModel.pcRepository.changed) {
            showConfirmationCancelDialog(
                FileAlertDialogType.SAVE_FILE,
                getString(R.string.just_no),
                {
                    viewModel.saveFile(mainActivityViewModel.currentDirectory.value!!)
                    transitionBackToFileFragment()
                },
                { transitionBackToFileFragment() })
        } else transitionBackToFileFragment()
    }

    private fun refreshRecyclerView(openedCategoryId: Int? = null) {
        requireView().view_pc_category_list.adapter = ViewPCRecyclerViewAdapter(
            viewModel.pcRepository.getCurrentFile(),
            this,
            openedCategoryId
        )
    }

    fun showConfirmationDialog(type: FileAlertDialogType, onConfirmation: () -> Unit) {
        showConfirmationCancelDialog(type, null, onConfirmation, {})
    }

    fun showConfirmationCancelDialog(
        type: FileAlertDialogType,
        cancelText: String?,
        onConfirmation: () -> Unit,
        onCancel: () -> Unit
    ) {
        dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(type.titleTextId))
            .setContentText(getString(type.contentText))
            .setCancelText(cancelText)
            .setConfirmText(getString(type.confirmText))
            .setConfirmClickListener {
                onConfirmation()
                dialog?.hide()
            }.setCancelClickListener {
                onCancel()
                dialog?.hide()
            }
        dialog?.show()
    }

    override fun openPairOptions(pair: PCPair) {
        bottomSheet = showActionPickerBottomSheet(
            options = getPairOptions(),
            onItemSelectedListener = OnItemSelectedListener {
                when (it.id) {
                    PairOptions.DELETE.id -> {
                        showConfirmationDialog(FileAlertDialogType.DELETE_PAIR) {
                            viewModel.pcRepository.deletePair(pair.id)
                            refreshRecyclerView(pair.categoryId)
                        }
                    }
                    PairOptions.EDIT.id -> {
                        transitionToEditPair(
                            EditPCPairParameter(
                                pair.categoryId,
                                pair.key,
                                pair.value,
                                pair.id
                            )
                        )
                    }
                    PairOptions.COPY_VALUE.id -> {
                        if (context == null) return@OnItemSelectedListener
                        val clipboardManager: ClipboardManager =
                            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(pair.key, pair.value)
                        clipboardManager.setPrimaryClip(clip)
                        Toast.makeText(
                            context,
                            R.string.copied_pc_pair_value_successful,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                bottomSheet?.dismiss()
            }
        )
    }

    override fun openCategoryOptions(category: PCCategory) {
        bottomSheet = showActionPickerBottomSheet(
            options = getCategoryOptions(),
            onItemSelectedListener = OnItemSelectedListener {
                when (it.id) {
                    CategoryOptions.EDIT.id -> {
                        transitionToEditCategory(
                            EditPCCategoryParameter(
                                category.name,
                                category.id
                            )
                        )
                    }
                    CategoryOptions.DELETE_ONLY_CAT.id -> {
                        showConfirmationDialog(FileAlertDialogType.DELETE_ONLY_CATEGORY) {
                            viewModel.pcRepository.deleteOnlyCategory(category.id)
                            refreshRecyclerView()
                        }
                    }
                    CategoryOptions.DELETE_CAT_AND_PAIRS.id -> {
                        showConfirmationDialog(FileAlertDialogType.DELETE_CATEGORY_AND_PAIRS) {
                            viewModel.pcRepository.deleteCategoryAndPairs(category.id)
                            refreshRecyclerView()
                        }
                    }
                }
                bottomSheet?.dismiss()
            }
        )
    }

    private fun transitionToEditCategory(args: EditPCCategoryParameter? = null) {
        resetBackPressedHandler()
        val action = ViewPCFragmentDirections.actionViewPCToEditPCCategory(args)
        findNavController(this).navigate(action)
    }

    private fun transitionToEditPair(args: EditPCPairParameter? = null) {
        resetBackPressedHandler()
        val action = ViewPCFragmentDirections.actionViewPCToEditPCPair(args)
        findNavController().navigate(action)
    }

    private fun transitionBackToFileFragment() {
        resetBackPressedHandler()
        val action = ViewPCFragmentDirections.actionViewPCToFileFragment()
        findNavController().navigate(action)
    }

    private fun getPairOptions(): List<Option> {
        return listOf(
            Option().apply {
                id = PairOptions.EDIT.id
                iconId = R.drawable.ic_baseline_edit_24
                title = getString(R.string.view_pc_option_edit_pair)
            },
            Option().apply {
                id = PairOptions.COPY_VALUE.id
                iconId = R.drawable.ic_baseline_content_paste_24
                title = getString(R.string.view_pc_option_pair_copy_value)
            },
            Option().apply {
                id = PairOptions.DELETE.id
                iconId = R.drawable.ic_baseline_delete_24
                title = getString(R.string.view_pc_option_delete_pair)
            }
        )
    }

    fun getCategoryOptions(): List<Option> {
        return listOf(
            Option().apply {
                id = CategoryOptions.EDIT.id
                iconId = R.drawable.ic_baseline_edit_24
                title = getString(R.string.view_pc_option_edit_category)
            },
            Option().apply {
                id = CategoryOptions.DELETE_ONLY_CAT.id
                iconId = R.drawable.ic_baseline_delete_24
                title = getString(R.string.view_pc_option_delete_only_category)
            },
            Option().apply {
                id = CategoryOptions.DELETE_CAT_AND_PAIRS.id
                iconId = R.drawable.ic_baseline_delete_sweep_24
                title = getString(R.string.view_pc_option_delete_category_and_pairs)
            })
    }

}