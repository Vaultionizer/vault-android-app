package com.vaultionizer.vaultapp.ui.main.pc

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.CreatePCViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreatePC : Fragment() {

    companion object {
        fun newInstance() = CreatePC()
    }

    private val viewModel: CreatePCViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_personal_container_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pcNameEdit = view.findViewById<EditText>(R.id.edit_pc_pair_value_input)
        val pcCreateButton = view.findViewById<MaterialButton>(R.id.create_pc_button)
        val pcInputLayout = view.findViewById<TextInputLayout>(R.id.edit_pc_pair_key_layout)
        val pcEditPCCheckbox = view.findViewById<CheckBox>(R.id.open_pc_checkbox)

        pcNameEdit.addTextChangedListener{
            viewModel.pcNameChanged(pcNameEdit.text.toString())
        }

        pcCreateButton.setOnClickListener {
            viewModel.createPersonalContainer(pcNameEdit.text.toString())
            viewModel.addTestData()
            var action: NavDirections;
            if (pcEditPCCheckbox.isChecked){
                action = CreatePCDirections.actionCreatePersonalContainerFragmentToViewPC()
            }
            else  {
                action = CreatePCDirections.actionCreateSpaceFragmentToFileFragment()
            }
            findNavController().navigate(action)
        }

        viewModel.pcCreationRes.observe(viewLifecycleOwner){
            pcCreateButton.isEnabled = it.isDataValid
            if (it.nameError != null){
                pcInputLayout.error = getString(it.nameError)
            }else{
                pcInputLayout.error = null;
            }
        }
    }
}