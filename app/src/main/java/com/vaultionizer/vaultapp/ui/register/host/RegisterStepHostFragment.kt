package com.vaultionizer.vaultapp.ui.register.host

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.vaultionizer.vaultapp.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterStepHostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterStepHostFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_step_host, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerHostEdit = view.findViewById<EditText>(R.id.register_host)
        registerHostEdit.setOnEditorActionListener { _, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_NEXT) {
                val action = RegisterStepHostFragmentDirections.actionRegisterStepHostFragmentToRegisterStepUserFragment3()
                findNavController().navigate(action)
            }

            false
        }
    }
}