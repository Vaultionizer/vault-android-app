package com.vaultionizer.vaultapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.repository.PCRepository
import com.vaultionizer.vaultapp.ui.main.pc.InputPCNameFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PCViewModel @Inject constructor(
    val pcRepository: PCRepository
) : ViewModel() {
    private val _pcCategoryNameRes = MutableLiveData<InputPCNameFormState>()
    val pcCategoryNameRes: LiveData<InputPCNameFormState> = _pcCategoryNameRes

    private val _pcPairRes = MutableLiveData<InputPCNameFormState>()
    val pcPairRes: LiveData<InputPCNameFormState> = _pcPairRes

    fun pairKeyHasChanged(key: String) {
        if (key.trim().isEmpty()) {
            _pcPairRes.value = InputPCNameFormState(R.string.error_input_pc_pair_key, false)
        } else {
            _pcPairRes.value = InputPCNameFormState(null, true)
        }
    }

    fun categoryChanged(text: String) {
        val content = text.trim()
        if (content.isEmpty()) {
            _pcCategoryNameRes.value =
                InputPCNameFormState(R.string.input_pc_category_too_short, false)
            return
        }
        if (pcRepository.findCategoryByName(content) != null) {
            _pcCategoryNameRes.value = InputPCNameFormState(R.string.error_create_category, false)
            return
        }

        _pcCategoryNameRes.value = InputPCNameFormState(null, true)
    }

    fun saveFile(parent: VNFile) {
        if (pcRepository.changed) {
            viewModelScope.launch { pcRepository.saveFile(parent) }
        }
    }
}