package com.vaultionizer.vaultapp.ui.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.repository.PCRepository
import com.vaultionizer.vaultapp.ui.main.pc.InputFormState

class PCViewModel @ViewModelInject constructor(
    val pcRepository: PCRepository
): ViewModel() {
    private val _pcCategoryNameRes = MutableLiveData<InputFormState>()
    val pcCategoryNameRes: LiveData<InputFormState> = _pcCategoryNameRes

    private val _pcPairRes = MutableLiveData<InputFormState>()
    val pcPairRes: LiveData<InputFormState> = _pcPairRes

    fun pairKeyHasChanged(key: String){
        if (key.trim().isEmpty()){
            _pcPairRes.value = InputFormState(R.string.error_input_pc_pair_key, false)
        }
        else{
            _pcPairRes.value = InputFormState(null, true)
        }
    }

    fun categoryChanged(text: String){
        val content = text.trim()
        if (content.isEmpty()){
            _pcCategoryNameRes.value = InputFormState(R.string.input_pc_category_too_short, false)
            return
        }
        if (pcRepository.findCategoryByName(content) != null){
            _pcCategoryNameRes.value = InputFormState(R.string.error_create_category, false)
            return
        }

        _pcCategoryNameRes.value = InputFormState(null, true)
    }
}