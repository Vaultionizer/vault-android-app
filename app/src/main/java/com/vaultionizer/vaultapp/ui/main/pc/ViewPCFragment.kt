package com.vaultionizer.vaultapp.ui.main.pc

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
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
import com.vaultionizer.vaultapp.ui.main.file.FileBottomSheetOption
import com.vaultionizer.vaultapp.ui.viewmodel.PCViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_pc_category_list.view.*

enum class PairOptions(val id: Long){
    DELETE(0),
    EDIT(1)
}

enum class CategoryOptions(val id: Long){
    EDIT(0),
    DELETE_ONLY_CAT(1),
    DELETE_CAT_AND_PAIRS(2)
}

@AndroidEntryPoint
class ViewPCFragment : Fragment(), ViewPCInterface {
    private var columnCount = 1
    private var dialog: SweetAlertDialog? = null
    private var bottomSheet: ActionPickerBottomSheet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    private val viewModel: PCViewModel by viewModels()

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
        refreshRecyclerView()
    }

    private fun refreshRecyclerView(openedCategoryId: Int? = null){
        requireView().view_pc_category_list.adapter = ViewPCRecyclerViewAdapter(viewModel.pcRepository.getCurrentFile(), this, openedCategoryId)
    }

    fun showConfirmationDialog(type: FileAlertDialogType, onConfirmation: () -> Unit) {
        dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(type.titleTextId))
            .setContentText(getString(type.contentText))
            .setConfirmText(getString(type.confirmText))
            .setConfirmClickListener {
                onConfirmation()
                dialog?.hide()
            }
        dialog?.show()
    }


    override fun openPairOptions(pair: PCPair) {
        bottomSheet = showActionPickerBottomSheet (
            options = getPairOptions(),
            onItemSelectedListener = OnItemSelectedListener {
                when(it.id){
                    PairOptions.DELETE.id -> {
                        showConfirmationDialog(FileAlertDialogType.DELETE_PAIR) {
                            viewModel.pcRepository.deletePair(pair.id)
                            refreshRecyclerView(pair.categoryId)
                        }
                    }
                    PairOptions.EDIT.id -> {
                        transitionToEditPair(EditPCPairParameter(pair.categoryId, pair.key, pair.value, pair.id))
                    }
                }
                bottomSheet?.dismiss()
            }
        )
    }

    override fun openCategoryOptions(category: PCCategory) {
        bottomSheet = showActionPickerBottomSheet (
            options = getCategoryOptions(),
            onItemSelectedListener = OnItemSelectedListener {
                when(it.id){
                    CategoryOptions.EDIT.id -> {
                        transitionToEditCategory(EditPCCategoryParameter(category.name, category.id))
                    }
                    CategoryOptions.DELETE_ONLY_CAT.id -> {
                        showConfirmationDialog(FileAlertDialogType.DELETE_ONLY_CATEGORY) {
                            viewModel.pcRepository.deleteOnlyCategory(category.id)
                            refreshRecyclerView()
                        }
                    }
                    CategoryOptions.DELETE_CAT_AND_PAIRS.id -> {
                        viewModel.pcRepository.deleteCategoryAndPairs(category.id)
                        refreshRecyclerView()
                    }
                }
                bottomSheet?.dismiss()
            }
        )
    }

    fun transitionToEditCategory(args: EditPCCategoryParameter? = null){
        val action = ViewPCFragmentDirections.actionViewPCToEditPCCategory(args)
        findNavController(this).navigate(action)
    }

    fun transitionToEditPair(args: EditPCPairParameter? = null){
        val action = ViewPCFragmentDirections.actionViewPCToEditPCPair(args)
        findNavController().navigate(action)
    }


    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ViewPCFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    private fun getPairOptions(): List<Option>{
        return listOf(
            Option().apply {
                id = PairOptions.DELETE.id
                iconId = R.drawable.ic_baseline_delete_24
                title = "Delete"
            },
            Option().apply {
                id = PairOptions.EDIT.id
                iconId = R.drawable.ic_baseline_delete_24
                title = "Edit"
            })
    }

    fun getCategoryOptions(): List<Option>{
        return listOf(
            Option().apply {
                id = CategoryOptions.EDIT.id
                iconId = R.drawable.ic_baseline_delete_24
                title = "Edit category"
            },
            Option().apply {
                id = CategoryOptions.DELETE_ONLY_CAT.id
                iconId = R.drawable.ic_baseline_delete_24
                title = "Delete only category"
            },
            Option().apply {
                id = CategoryOptions.DELETE_CAT_AND_PAIRS.id
                iconId = R.drawable.ic_baseline_delete_24
                title = "Delete category and child pairs"
            })
    }

}