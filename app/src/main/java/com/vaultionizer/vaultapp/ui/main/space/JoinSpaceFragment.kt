package com.vaultionizer.vaultapp.ui.main.space

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.JoinSpaceViewModel
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoinSpaceFragment : Fragment() {
    private val viewModel: JoinSpaceViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val args: JoinSpaceFragmentArgs by navArgs()
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_space, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.reset()
        val joinSpaceButton = view.findViewById<Button>(R.id.join_space_btn)
        val joinSpaceInput = view.findViewById<TextInputEditText>(R.id.input_auth_key_name)
        val joinSpaceInputLayout = view.findViewById<TextInputLayout>(R.id.input_auth_key_layout)

        joinSpaceInput.addTextChangedListener {
            viewModel.checkWellFormed(joinSpaceInput.text.toString())
        }

        joinSpaceInputLayout.setEndIconOnClickListener {
            findNavController().navigate(JoinSpaceFragmentDirections.actionJoinSpaceFragmentToQRCodeScanFragment())
        }
        if (args.authData != null) {
            Toast.makeText(context, R.string.scan_qr_code_successful, Toast.LENGTH_SHORT).show()
            view.findViewById<TextInputEditText>(R.id.input_auth_key_name).setText(args.authData)
        }

        viewModel.doneTestingJoinSpace.observe(viewLifecycleOwner) {
            if (it.started && !it.done) {
                progressDialog = ProgressDialog.show(
                    context, "",
                    getString(R.string.trying_to_join_space), true
                )
            } else {
                progressDialog?.dismiss()
            }
            if (it.success) {
                Toast.makeText(context, R.string.join_space_success_toast, Toast.LENGTH_SHORT)
                    .show()
                mainActivityViewModel.updateUserSpaces()
                findNavController().navigate(JoinSpaceFragmentDirections.actionJoinSpaceFragmentToFileFragment(false))
            }
        }

        joinSpaceButton.setOnClickListener {
            MaterialDialog(requireContext()).show {
                input { dialog, text ->
                    viewModel.joinSpace(joinSpaceInput.text.toString(), text.toString())
                }
                positiveButton(R.string.join_space_password_button)
                title(R.string.join_space_password_title)
            }

        }

        viewModel.joinSpaceInputState.observe(viewLifecycleOwner) {
            joinSpaceButton.isEnabled = it.wellFormed
            joinSpaceInputLayout.error =
                if (it.wellFormed || it.statusText == null) "" else getString(it.statusText)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = JoinSpaceFragment()
    }
}