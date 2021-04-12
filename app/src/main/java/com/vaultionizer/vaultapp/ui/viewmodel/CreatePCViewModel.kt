package com.vaultionizer.vaultapp.ui.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.repository.PCRepository
import com.vaultionizer.vaultapp.ui.main.pc.InputFormState

class CreatePCViewModel @ViewModelInject constructor(
    val pcRepository: PCRepository
): ViewModel() {
    private val _pcCreationRes = MutableLiveData<InputFormState>()
    val pcCreationRes: LiveData<InputFormState> = _pcCreationRes


    fun pcNameChanged(text: String){
        val name = text.trim()

        if (name.length < 4){
            _pcCreationRes.value = InputFormState(R.string.create_pc_too_short_name, false)
        }
        else{
            _pcCreationRes.value = InputFormState(null,true)
        }
    }

    fun createPersonalContainer(name: String){
        pcRepository.createNewFile(name)
    }
}