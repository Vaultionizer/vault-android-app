package com.vaultionizer.vaultapp.ui.viewmodel

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.rf.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.rf.NetworkFolder
import com.vaultionizer.vaultapp.data.model.rest.rf.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.space.NetworkSpace
import com.vaultionizer.vaultapp.repository.ReferenceFileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class MainActivityViewModel @ViewModelInject constructor(val spaceRepository: SpaceRepository, val referenceFileRepository: ReferenceFileRepository): ViewModel() {

    private val _userSpaces = MutableLiveData<List<NetworkSpace>>()
    val userSpaces: LiveData<List<NetworkSpace>> = _userSpaces

    private val _selectedSpace = MutableLiveData<NetworkSpace>()
    val selectedSpace: LiveData<NetworkSpace> = _selectedSpace

    private val _currentReferenceFile = MutableLiveData<NetworkReferenceFile>()
    val currentReferenceFile: LiveData<NetworkReferenceFile> = _currentReferenceFile

    private val _folderHierarchy = MutableLiveData<LinkedList<NetworkFolder>>(LinkedList())
    val folderHierarchy: LiveData<LinkedList<NetworkFolder>> = _folderHierarchy // TODO(jatsqi): Mutable violates SSOT here

    private val _shownElements = MutableLiveData<List<NetworkElement>>()
    val shownElements: LiveData<List<NetworkElement>> = _shownElements

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
                    //_currentReferenceFile.value = it.data
                    _currentReferenceFile.value = NetworkReferenceFile.generateRandom()
                    Log.i("Vault", "Ref file success!")
                } else if(it is ManagedResult.Error) {
                    Log.e("Vault", "Ref file error: ${it.statusCode.toString()}")
                }
            }
        }
    }

    fun selectedSpaceChanged(space: NetworkSpace) {
        _selectedSpace.value = space
        updateCurrentReferenceFile()
    }

    fun onDirectoryChange(newFolder: NetworkFolder?): NetworkFolder? {
        val list = _folderHierarchy.value
        var result = newFolder

        if (newFolder == null) {
            if(list?.isEmpty() == true) {
                result = null
            } else {
                result = list?.removeLast()
            }
        } else {
            list?.add(newFolder)
        }

        _folderHierarchy.value = list
        _shownElements.value = result?.content ?: currentReferenceFile.value!!.elements

        return result
    }

    fun onSearchQuery(query: String) {

    }

    private fun collectRecursive(query: String, result: MutableList<NetworkElement>, current: NetworkFolder) {

    }
}