package com.vaultionizer.vaultapp.ui.main.pc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.PCViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditPCCategoryFragment : Fragment() {

    companion object {
        fun newInstance() = EditPCCategoryFragment()
    }

    private val viewModel: PCViewModel by viewModels()
    private val args: EditPCCategoryFragmentArgs? by navArgs()
    private var editMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.edit_pc_category_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val categoryInput = view.findViewById<EditText>(R.id.edit_pc_category_input)
        val pcCatInputLayout = view.findViewById<TextInputLayout>(R.id.edit_pc_category_layout)
        val createButton = view.findViewById<Button>(R.id.edit_pc_category_button)

        categoryInput.addTextChangedListener {
            viewModel.categoryChanged(categoryInput.text.toString())
        }

        createButton.setOnClickListener {
            if (editMode) {
                val res = viewModel.pcRepository.addCategory(
                    categoryInput.text.toString(),
                    args!!.parameters?.categoryId
                )
                if (!res) {
                    Toast.makeText(context, R.string.error_toast_edit_category, Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
            } else {
                val res = viewModel.pcRepository.addCategory(categoryInput.text.toString())
                if (!res) {
                    Toast.makeText(
                        context,
                        R.string.error_toast_create_category,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }
            val action = EditPCCategoryFragmentDirections.actionEditPCCategoryToViewPC()
            findNavController().navigate(action)
        }

        if (args != null && args!!.parameters != null) {
            editMode = true
            categoryInput.setText(args!!.parameters!!.categoryName)
            createButton.setText(R.string.button_pc_edit_category_name)
        }

        viewModel.pcCategoryNameRes.observe(viewLifecycleOwner) {
            createButton.isEnabled = it.isDataValid
            if (it.nameError != null) {
                pcCatInputLayout.error = getString(it.nameError)
            } else {
                pcCatInputLayout.error = null
            }
        }
    }


}