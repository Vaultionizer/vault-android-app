package com.vaultionizer.vaultapp.ui.viewmodel

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.repository.ReferenceFileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class MainActivityViewModel @ViewModelInject constructor(val spaceRepository: SpaceRepository, val fileRepository: FileRepository): ViewModel() {

    private val _userSpaces = MutableLiveData<List<VNSpace>>()
    val userSpaces: LiveData<List<VNSpace>> = _userSpaces

    private val _selectedSpace = MutableLiveData<VNSpace>()
    val selectedSpace: LiveData<VNSpace> = _selectedSpace

    private val _shownElements = MutableLiveData<List<VNFile>>()
    val shownElements: LiveData<List<VNFile>> = _shownElements

    private val _currentDirectory = MutableLiveData<VNFile>()
    val currentDirectory: LiveData<VNFile> = _currentDirectory

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

    private fun updateCurrentFiles() {
        viewModelScope.launch {
            if(selectedSpace.value == null) {
                Log.e("Vault", "Space is null")
                return@launch
            }

            val elements = fileRepository.getSpaceFiles(selectedSpace.value!!)
            elements.collect {
                when(it) {
                    is ManagedResult.Success -> {
                        Log.e("Vault", "SUCCESS!!!!")

                        val currentParent = if(_currentDirectory.value == null) {
                            it.data.values.filter {
                                it.isFolder && it.parentId == null
                            }.first().localId
                        } else {
                            _currentDirectory.value!!.localId
                        }

                        val shown = it.data.values.filter { it.parentId != null && it.parentId == currentParent }
                        _shownElements.value = shown

                        Log.e("Vault", "SIZE: ${shown.size} BY PARENT $currentParent")
                    }
                    else -> {
                        Log.e("Vault", "NOOOOOO ${it.javaClass.name}")
                    }
                }
            }
        }
    }

    fun selectedSpaceChanged(space: VNSpace) {
        Log.e("Vault", "Change space...")
        _selectedSpace.value = space
        updateCurrentFiles()
    }

    fun onDirectoryChange(newFolder: VNFile?) {
        Log.d("Vault", "Change")
        if(_selectedSpace.value == null) return
        if(_currentDirectory.value == null && newFolder == null) return

        viewModelScope.launch {
            val elements = fileRepository.getSpaceFiles(selectedSpace.value!!)

            elements.collect {
                when(it) {
                    is ManagedResult.Success -> {
                        if(newFolder == null) {
                            _currentDirectory.value = it.data[_currentDirectory.value!!.parentId]
                        } else {
                            _currentDirectory.value = it.data[newFolder.localId]
                        }

                        updateCurrentFiles()
                    }
                }
            }
        }
    }

    fun onSearchQuery(query: String) {

    }

}