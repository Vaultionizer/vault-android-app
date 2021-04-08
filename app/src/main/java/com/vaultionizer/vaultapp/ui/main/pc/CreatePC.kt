package com.vaultionizer.vaultapp.ui.main.pc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.CreatePCViewModel

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

        val pcNameEdit = view.findViewById<EditText>(R.id.input_pc_name)
        val pcCreateButton = view.findViewById<MaterialButton>(R.id.create_pc_button)
        val pcInputLayout = view.findViewById<TextInputLayout>(R.id.input_pc_layout)
        val pcEditPCCheckbox = view.findViewById<CheckBox>(R.id.open_pc_checkbox)

        pcNameEdit.addTextChangedListener{
            viewModel.pcNameChanged(pcNameEdit.text.toString())
        }

        pcCreateButton.setOnClickListener {
            if (viewModel.pcCreationRes.value != null && viewModel.pcCreationRes.value!!.isDataValid){
                viewModel.createPersonalContainer(pcNameEdit.text.toString())
                if (pcEditPCCheckbox.isSelected){
                    // open created PC in editing mode
                    // TODO(keksklauer4):
                }
            }
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