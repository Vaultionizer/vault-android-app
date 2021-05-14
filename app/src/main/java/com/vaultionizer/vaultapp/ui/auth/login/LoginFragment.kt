package com.vaultionizer.vaultapp.ui.auth.login

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.thedeanda.lorem.LoremIpsum
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.db.dao.LocalUserDao
import com.vaultionizer.vaultapp.ui.auth.data.AuthEvent
import com.vaultionizer.vaultapp.ui.auth.data.AuthViewModel
import com.vaultionizer.vaultapp.ui.auth.parts.input.HostInputFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var userService: LocalUserDao

    private var hostValid = false
    private var userDataValid = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel.resetState()

        val hostInputFragment =
            childFragmentManager.findFragmentById(R.id.fragment_part_host_login) as HostInputFragment
        val editText = view.findViewById<EditText>(R.id.input_host)

        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                hostInputFragment.triggerHostValidation(true)
                true
            }
            false
        }

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hostInputFragment.triggerHostValidation(true)
            }
        }

        val loginButton = view.findViewById<Button>(R.id.login)
        loginButton.attachTextChangeAnimator()
        loginButton.setOnClickListener {
            loginButton.isClickable = false
            authViewModel.loginWithFormData()
        }

        authViewModel.authenticationEvent.observe(viewLifecycleOwner, {
            when (it) {
                is AuthEvent.LoginValidation -> {
                    when {
                        it.isLoading == true -> {
                            loginButton.showProgress {
                                buttonTextRes = R.string.login_login_progress
                                progressColor = Color.WHITE
                            }
                        }
                        it.error == null -> {
                            val action =
                                LoginFragmentDirections.actionLoginFragmentToMainActivity2()
                            findNavController().navigate(action)
                            activity?.finish()
                        }
                        else -> {
                            if (requireContext() != null) {
                                Toast.makeText(requireContext(), it.error!!, Toast.LENGTH_LONG)
                                    .show()
                            }
                            loginButton.isClickable = true
                            loginButton.hideProgress(R.string.login_login)
                        }
                    }
                }

                is AuthEvent.UserDataValidation -> userDataValid = it.isDataValid
                is AuthEvent.HostValidation -> hostValid = it.error == null
            }

            loginButton.isEnabled = hostValid && userDataValid
        })

        val signUpButton = view.findViewById<Button>(R.id.login_sign_up)
        signUpButton.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterStepHostFragment()
            findNavController().navigate(action)
        }
        authViewModel.userDataChanged(LoremIpsum.getInstance().getWords(5), "djsfjjdjf+++!!fsdfsDDDD")
        authViewModel.hostDataChanged("api.vault.jatsqi.com")
        authViewModel.registerWithFormData()
    }

}
