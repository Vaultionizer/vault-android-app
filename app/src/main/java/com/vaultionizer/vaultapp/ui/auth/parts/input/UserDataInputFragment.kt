package com.vaultionizer.vaultapp.ui.auth.parts.input

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.auth.data.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDataInputFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_part_user_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEdit = view.findViewById<EditText>(R.id.input_username)
        val passwordEdit = view.findViewById<EditText>(R.id.input_password)

        usernameEdit.addTextChangedListener {
            authViewModel.usernameDataChanged(usernameEdit.text.toString())
        }

        passwordEdit.addTextChangedListener {
            authViewModel.passwordDataChanged(passwordEdit.text.toString())
        }
    }

}