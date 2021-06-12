package com.vaultionizer.vaultapp.ui.auth.register

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.auth.data.AuthEvent
import com.vaultionizer.vaultapp.ui.auth.data.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Retrofit
import javax.inject.Inject

@AndroidEntryPoint
class RegisterStepUserFragment : Fragment() {

    @Inject
    lateinit var retrofit: Retrofit
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_step_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("VAULT", retrofit.baseUrl().toString())

        val registerUserPasswordEdit = view.findViewById<EditText>(R.id.input_password)
        val registerUserEdit = view.findViewById<EditText>(R.id.input_username)
        val continueButton = view.findViewById<Button>(R.id.button_continue_user_data)

        continueButton.isEnabled = false

        registerUserPasswordEdit.addTextChangedListener {
            authViewModel.userDataChanged(password = registerUserPasswordEdit.text.toString())
        }

        registerUserEdit.addTextChangedListener {
            authViewModel.userDataChanged(username = registerUserEdit.text.toString())
        }

        authViewModel.authenticationEvent.observe(viewLifecycleOwner) {
            if (it is AuthEvent.UserDataValidation) {
                continueButton.isEnabled = it.isDataValid
            }
        }

        continueButton.setOnClickListener {
            val action =
                RegisterStepUserFragmentDirections.actionRegisterStepUserFragmentToRegisterStepAuthKeyFragment()
            findNavController().navigate(action)
        }
    }
}