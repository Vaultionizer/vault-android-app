package com.vaultionizer.vaultapp.ui.main.pc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.PCViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditPCPairFragment : Fragment() {

    companion object {
        fun newInstance() = EditPCPairFragment()
    }

    private val viewModel: PCViewModel by viewModels()
    private val args: EditPCPairFragmentArgs by navArgs()

    private var editMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.edit_pc_pair_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val keyInput = view.findViewById<EditText>(R.id.edit_pc_pair_key_input)
        val valInput = view.findViewById<EditText>(R.id.edit_pc_pair_value_input)
        val pcKeyInputLayout = view.findViewById<TextInputLayout>(R.id.edit_pc_pair_key_layout)
        val createButton = view.findViewById<Button>(R.id.edit_pc_pair_button)

        val spinner = view.findViewById<Spinner>(R.id.edit_pc_pair_category_spinner)
        spinner.adapter = ArrayAdapter<String>(
            requireContext(), R.layout.support_simple_spinner_dropdown_item,
            viewModel.pcRepository.getCatgoryNames()
        )

        keyInput.addTextChangedListener {
            viewModel.pairKeyHasChanged(keyInput.text.toString())
        }

        createButton.setOnClickListener {
            // write to pc repository
            if (editMode) {
                viewModel.pcRepository.replacePair(
                    keyInput.text.toString(),
                    valInput.text.toString(),
                    viewModel.pcRepository.getCategoryIdByPos(spinner.selectedItemPosition - 1),
                    args.parameters?.id!!
                )
            } else {
                viewModel.pcRepository.addNewPair(
                    keyInput.text.toString(), valInput.text.toString(),
                    viewModel.pcRepository.getCategoryIdByPos(spinner.selectedItemPosition - 1)
                )
            }
            val action = EditPCPairFragmentDirections.actionEditPCPairToViewPC()
            findNavController().navigate(action)
        }

        if (args.parameters != null) {
            editMode = true
            keyInput.setText(args.parameters!!.key)
            valInput.setText(args.parameters!!.value)
            createButton.setText(R.string.button_create_pc_pair_edit)
            spinner.setSelection(viewModel.pcRepository.getCategoryPosById(args.parameters!!.category))
        }

        viewModel.pcPairRes.observe(viewLifecycleOwner) {
            createButton.isEnabled = it.isDataValid
            if (it.nameError != null) {
                pcKeyInputLayout.error = getString(it.nameError)
            } else {
                pcKeyInputLayout.error = null
            }
        }
    }
}