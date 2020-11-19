package com.vaultionizer.vaultapp.ui.viewmodel

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.rf.ReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.space.SpaceEntry
import com.vaultionizer.vaultapp.repository.ReferenceFileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivityViewModel @ViewModelInject constructor(val spaceRepository: SpaceRepository, val referenceFileRepository: ReferenceFileRepository): ViewModel() {

    private val _userSpaces = MutableLiveData<List<SpaceEntry>>()
    val userSpaces: LiveData<List<SpaceEntry>> = _userSpaces

    private val _selectedSpace = MutableLiveData<SpaceEntry>()
    val selectedSpace: LiveData<SpaceEntry> = _selectedSpace

    private val _currentReferenceFile = MutableLiveData<ReferenceFile>()
    val currentReferenceFile: LiveData<ReferenceFile> = _currentReferenceFile

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

    private fun updateCurrentReferenceFile() {
        viewModelScope.launch {
            val result = referenceFileRepository.downloadReferenceFile(selectedSpace.value!!.spaceID)

            result.collect {
                if(it is ManagedResult.Success) {
                    _currentReferenceFile.value = it.data
                    Log.i("Vault", "Ref file success!")
                } else if(it is ManagedResult.Error) {
                    Log.e("Vault", "Ref file error: ${it.statusCode.toString()}")
                }
            }
        }
    }

    fun selectedSpaceChanged(space: SpaceEntry) {
        _selectedSpace.value = space
        updateCurrentReferenceFile()
    }

}