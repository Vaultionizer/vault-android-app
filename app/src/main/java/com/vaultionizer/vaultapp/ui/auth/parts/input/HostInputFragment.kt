package com.vaultionizer.vaultapp.ui.auth.parts.input

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.auth.data.AuthViewModel
import com.vaultionizer.vaultapp.util.extension.getProgressBarDrawable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostInputFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_part_host, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.hostFormState.observe(viewLifecycleOwner, Observer {
            val editLayout = requireView().findViewById<TextInputLayout>(R.id.input_host_layout)

            if (!it.hostValid) {
                Log.e("Vault", "Host not valid!")
                editLayout.error = it.hostError
                editLayout.endIconDrawable = null
            } else {
                editLayout.error = null
                if (editLayout.endIconDrawable != null) {
                    editLayout.endIconDrawable =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_outline_done_24)
                }
            }
        })

        val hostText = view.findViewById<EditText>(R.id.input_host)
        hostText.addTextChangedListener {
            authViewModel.hostDataChanged(hostText.text.toString())
        }
    }

    fun triggerHostValidation(triggerAnimation: Boolean) {
        val editText = requireView().findViewById<EditText>(R.id.input_host)
        val editLayout = requireView().findViewById<TextInputLayout>(R.id.input_host_layout)

        if (triggerAnimation) {
            editLayout.endIconDrawable = requireContext().getProgressBarDrawable()
            (editLayout.endIconDrawable as Animatable).start()
        }

        authViewModel.validateHost(editText.text.toString())
    }
}