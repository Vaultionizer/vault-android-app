package com.vaultionizer.vaultapp.ui.main.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.viewmodel.FileStatusViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FileStatusFragment : Fragment() {

    private val statusViewModel: FileStatusViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}