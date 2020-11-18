package com.vaultionizer.vaultapp.ui.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.auth.data.AuthViewModel

class RegisterStepAuthKeyFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_step_auth_key, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authKeyEdit = view.findViewById<EditText>(R.id.register_auth_key)
        authKeyEdit.addTextChangedListener {
            authViewModel.authKeyDataChanged(authKeyEdit.text.toString())
        }

        val finishButton = view.findViewById<Button>(R.id.button_finish)
        finishButton.setOnClickListener {
            authViewModel.registerWithFormData()
        }

        authViewModel.loginResult.observe(viewLifecycleOwner, Observer {
            if(it.error == null) {
                val action =
                    RegisterStepAuthKeyFragmentDirections.actionRegisterStepAuthKeyFragmentToMainActivity2()
                findNavController().navigate(action)
            }
        })
    }

}