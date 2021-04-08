package com.vaultionizer.vaultapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.main.pc.CreatePCFormState
import com.vaultionizer.vaultapp.ui.main.space.SpaceCreationResult

class CreatePCViewModel : ViewModel() {
    private val _pcCreationRes = MutableLiveData<CreatePCFormState>()
    val pcCreationRes: LiveData<CreatePCFormState> = _pcCreationRes

    fun createPersonalContainer(name: String){

    }

    fun pcNameChanged(text: String){
        val name = text.trim()

        if (name.length < 4){
            _pcCreationRes.value = CreatePCFormState(R.string.create_pc_too_short_name, false)
        }
        else{
            _pcCreationRes.value = CreatePCFormState(null,true)
        }
    }
}