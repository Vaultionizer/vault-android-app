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
import androidx.navigation.fragment.findNavController
import com.nambimobile.widgets.efab.ExpandableFabLayout
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.PCViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_pc_category_list.view.*


@AndroidEntryPoint
class ViewPCFragment : Fragment() {
    private var columnCount = 1

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
        if (view.view_pc_category_list is RecyclerView) {
            with(view.view_pc_category_list) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = ViewPCRecyclerViewAdapter(viewModel.pcRepository.getCurrentFile())
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

                else -> {
                }
            }
        }
        with(view.view_pc_category_list) {
            adapter = ViewPCRecyclerViewAdapter(viewModel.pcRepository.getCurrentFile())
        }
    }

    fun transitionToEditCategory(){
        val action = ViewPCFragmentDirections.actionViewPCToEditPCCategory();
        findNavController().navigate(action);
    }

    fun transitionToEditPair(){
        val action = ViewPCFragmentDirections.actionViewPCToEditPCPair();
        findNavController().navigate(action);
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
}