package com.vaultionizer.vaultapp.ui.auth.parts.alternative

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.vaultionizer.vaultapp.NavigationDirections
import com.vaultionizer.vaultapp.R

class RegisterSignUpAlternativeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_alternative, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button = view.findViewById<Button>(R.id.register_sign_in)
        button.setOnClickListener {
            view.findNavController().navigate(NavigationDirections.actionGlobalLoginFragment())
        }
    }
}