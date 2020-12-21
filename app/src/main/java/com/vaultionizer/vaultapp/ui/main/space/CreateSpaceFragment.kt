package com.vaultionizer.vaultapp.ui.main.space

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.textfield.TextInputLayout
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.CreateSpaceViewModel
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateSpaceFragment : Fragment() {

    private val viewModel: CreateSpaceViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_space, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner = view.findViewById<Spinner>(R.id.input_space_algorithm)
        spinner.adapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item, arrayOf(
            "AES256 GCM",
            "AES256 CBC"
        ))

        val spaceNameLayout = view.findViewById<TextInputLayout>(R.id.input_space_layout)
        val spaceNameEdit = view.findViewById<EditText>(R.id.input_space_name)
        val spaceShared = view.findViewById<CheckBox>(R.id.input_space_shared)
        val createButton = view.findViewById<Button>(R.id.create_space)
        createButton.isEnabled = false
        createButton.attachTextChangeAnimator()

        createButton.setOnClickListener {
            createButton.showProgress {
                buttonTextRes = R.string.create_space_progress
                progressColor = Color.WHITE
            }
            viewModel.createSpace(spaceNameEdit.text.toString(), !spaceShared.isChecked, spinner.selectedItem.toString())
        }

        spaceNameEdit.addTextChangedListener {
            viewModel.spaceNameChanged(spaceNameEdit.text.toString())
        }

        viewModel.spaceFormState.observe(viewLifecycleOwner) {
            if(!it.isDataValid) {
                createButton.isEnabled = false
                if(spaceNameLayout.error == null) {
                    if(it.nameError != null) {
                        spaceNameLayout.error = getString(it.nameError)
                    }
                }

                if(it.nameError == null) {
                    spaceNameLayout.error = null
                }
            } else {
                createButton.isEnabled = true
                spaceNameLayout.error = null
            }
        }

        viewModel.spaceCreationResult.observe(viewLifecycleOwner) {
            if(it.creationSuccessful) {
                createButton.hideProgress(R.string.create_space_create)

                mainActivityViewModel.updateUserSpaces()
                mainActivityViewModel.selectedSpaceChanged(it.createdSpace!!)

                val action = CreateSpaceFragmentDirections.actionCreateSpaceFragmentToFileFragment()
                findNavController().navigate(action)
            }
        }
    }

}