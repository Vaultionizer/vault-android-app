package com.vaultionizer.vaultapp.ui.main.space

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vaultionizer.vaultapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoinSpaceFragment : Fragment() {
    private val args: JoinSpaceFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_space, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inputAuthKeyLayout = view.findViewById<TextInputLayout>(R.id.input_auth_key_layout)
        inputAuthKeyLayout.setEndIconOnClickListener{
            findNavController().navigate(JoinSpaceFragmentDirections.actionJoinSpaceFragmentToQRCodeScanFragment())
        }
        if(args.authData != null){
            view.findViewById<TextInputEditText>(R.id.input_auth_key_name).setText(args.authData)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JoinSpaceFragment()
    }
}