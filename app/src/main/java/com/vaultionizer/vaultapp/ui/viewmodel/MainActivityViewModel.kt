package com.vaultionizer.vaultapp.ui.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.space.SpaceEntry
import com.vaultionizer.vaultapp.repository.SpaceRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivityViewModel @ViewModelInject constructor(val spaceRepository: SpaceRepository): ViewModel() {

    private val _userSpaces = MutableLiveData<List<SpaceEntry>>()
    val userSpaces: LiveData<List<SpaceEntry>> = _userSpaces

    fun updateUserSpaces() {
        viewModelScope.launch {
            val result = spaceRepository.getAllSpaces()

            result.collect {
                if(it is ManagedResult.Success) {
                    _userSpaces.value = it.data
                }
            }
        }
    }

}