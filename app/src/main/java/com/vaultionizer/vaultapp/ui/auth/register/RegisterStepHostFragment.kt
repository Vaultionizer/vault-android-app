package com.vaultionizer.vaultapp.ui.auth.register

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.*
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.auth.data.AuthEvent
import com.vaultionizer.vaultapp.ui.auth.data.AuthViewModel
import com.vaultionizer.vaultapp.ui.auth.parts.input.HostInputFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterStepHostFragment : Fragment() {

    val authViewModel: AuthViewModel by activityViewModels()

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

        val hostInputFragment =
            childFragmentManager.findFragmentById(R.id.fragment_part_host) as HostInputFragment
        val continueButton = view.findViewById<Button>(R.id.button_continue)

        bindProgressButton(continueButton)
        continueButton.attachTextChangeAnimator()

        authViewModel.authenticationEvent.observe(viewLifecycleOwner, {
            if (it is AuthEvent.HostValidation) {
                val continueEnabled = it.error == null
                if (continueButton.isEnabled != continueEnabled) {
                    continueButton.isEnabled = continueEnabled
                }

                if (it.version != null) {
                    val action =
                        RegisterStepHostFragmentDirections.actionRegisterStepHostFragmentToRegisterStepUserFragment3()
                    findNavController().navigate(action)
                }

                if (!it.isLoading && continueButton.isProgressActive()) {
                    continueButton.hideProgress(R.string.all_continue)
                }
            }
        })

        continueButton.isEnabled = false
        continueButton.setOnClickListener {
            hostInputFragment.triggerHostValidation(false)

            continueButton.showProgress {
                buttonTextRes = R.string.register_step_host_loading
                progressColor = Color.WHITE
            }
        }
    }
}