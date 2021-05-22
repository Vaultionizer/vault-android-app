package com.vaultionizer.vaultapp.ui.main.keys

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.cryptography.CryptoUtils
import com.vaultionizer.vaultapp.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KeyManagementFragment : Fragment() {

    private val viewModel: MainActivityViewModel by activityViewModels()

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

        list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        list.adapter = KeyManagementAdapter {
            CryptoUtils.deleteKey(it.id)

            val adapter = list.adapter
            if (adapter != null) {
                if (adapter is KeyManagementAdapter) {
                    adapter.updateSpaces(viewModel.userSpaces.value!!.filter {
                        CryptoUtils.existsKey(
                            it.id
                        )
                    }.toMutableList())
                }
            }
        }

        viewModel.userSpaces.observe(viewLifecycleOwner) {
            val adapter = list.adapter
            if (adapter != null) {
                if (adapter is KeyManagementAdapter) {
                    adapter.updateSpaces(it.filter { CryptoUtils.existsKey(it.id) }
                        .toMutableList())
                }
            }
        }
    }
}