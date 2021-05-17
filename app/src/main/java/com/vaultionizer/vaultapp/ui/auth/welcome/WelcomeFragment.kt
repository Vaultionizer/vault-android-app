package com.vaultionizer.vaultapp.ui.auth.welcome

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.auth.AuthenticationActivity

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(false)
        (activity as AppCompatActivity).supportActionBar?.hide()

        view.findViewById<Button>(R.id.button_lets_go).setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin(){
        val action = WelcomeFragmentDirections.actionWelcomeFragmentToLoginFragment()
        findNavController().navigate(action)
    }
}