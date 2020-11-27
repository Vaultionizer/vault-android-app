package com.vaultionizer.vaultapp.ui.main.keys

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.cryptography.Cryptography
import kotlinx.android.synthetic.main.fragment_key_management.*


class KeyManagementFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_key_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<RecyclerView>(R.id.list_key)
        list.adapter = KeyManagementAdapter(arrayOf("Key1", "Key2", "Key4"), {Cryptography().deleteKey(it.spaceID)})
        list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
}