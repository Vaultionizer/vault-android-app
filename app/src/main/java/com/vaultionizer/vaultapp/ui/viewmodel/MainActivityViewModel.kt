package com.vaultionizer.vaultapp.ui.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.cryptography.CryptoUtils
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import com.vaultionizer.vaultapp.data.live.NetworkLiveData
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.ui.main.file.FileEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val spaceRepository: SpaceRepository,
    val fileRepository: FileRepository
) : ViewModel() {

    private val _userSpaces = MutableLiveData<List<VNSpace>>()
    val userSpaces: LiveData<List<VNSpace>> = _userSpaces

    private val _selectedSpace = MutableLiveData<VNSpace>()
    val selectedSpace: LiveData<VNSpace> = _selectedSpace

    private val _shownElements = MutableLiveData<List<VNFile>>()
    val shownElements: LiveData<List<VNFile>> = _shownElements

    private val _currentDirectory = MutableLiveData<VNFile>()
    val currentDirectory: LiveData<VNFile> = _currentDirectory

    private val _fileEvent = MutableLiveData<FileEvent>()
    val fileEvent: LiveData<FileEvent> = _fileEvent

    val networkStatus =
        NetworkLiveData(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

    init {
        updateUserSpaces()
    }

    fun updateUserSpaces() {
        viewModelScope.launch {
            val result = spaceRepository.getAllSpaces()

            // TODO(jatsqi): Error handling
            result.collect {
                if (it is Resource.Success) {
                    _userSpaces.value = it.data

                    val lastAccessedSpace =
                        it.data.sortedByDescending { it.lastAccess }.firstOrNull()
                    lastAccessedSpace?.let { space ->
                        selectedSpaceChanged(space)
                    }
                }
            }
        }
    }

    fun generateSpaceKey(space: VNSpace) {
        CryptoUtils.generateKeyForSingleUserSpace(
            space.id,
            CryptoType.AES,
            CryptoMode.GCM,
            CryptoPadding.NoPadding
        )
    }

    private fun updateCurrentFiles() {
        viewModelScope.launch {
            if (_selectedSpace.value == null) {
                return@launch
            }

            if (_currentDirectory.value != null) {
                _shownElements.value = _currentDirectory.value!!.content
            } else {
                val response = fileRepository.getFileTree(_selectedSpace.value!!)

                response.collect {
                    when (it) {
                        is Resource.Success -> {
                            _currentDirectory.value = it.data
                            updateCurrentFiles()
                        }
                        is Resource.CryptographicalError -> {
                            _fileEvent.postValue(
                                FileEvent.EncryptionKeyRequired(
                                    selectedSpace.value!!
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun requestUpload(uri: Uri) {
        viewModelScope.launch {
            fileRepository.uploadFile(
                uri,
                _currentDirectory.value!!,
            )
        }
    }

    fun requestDownload(file: VNFile) {
        viewModelScope.launch {
            fileRepository.downloadFile(file)
        }
    }

    fun requestDecryption(file: VNFile) {
        viewModelScope.launch {
            fileRepository.decryptFile(file)
        }
    }

    fun requestFolder(name: String) {
        if (_selectedSpace.value != null && _currentDirectory.value != null) {
            viewModelScope.launch {
                fileRepository.uploadFolder(name, _currentDirectory.value!!)
                updateCurrentFiles()
            }
        }
    }

    fun requestDeletion(file: VNFile) {
        viewModelScope.launch {
            fileRepository.deleteFile(file)
            updateCurrentFiles()
        }
    }

    fun requestSpaceDeletion() {
        if (_userSpaces.value?.size?.minus(1) == 0) return
        viewModelScope.launch {
            spaceRepository.deleteSpace(_selectedSpace.value!!).collect {
                when (it) {
                    is Resource.Success -> {
                        val spaces = _userSpaces.value!!.toMutableList()
                        spaces.remove(it)

                        _selectedSpace.value = spaces[0]
                        _currentDirectory.value = null
                        updateUserSpaces()
                        updateCurrentFiles()
                    }
                }
            }
        }
    }

    fun requestQuitSpace() {
        // TODO
    }

    fun selectedSpaceChanged(space: VNSpace): Boolean {
        Log.e("Vault", "Change space...")
        if (!space.isKeyAvailable) {
            _fileEvent.postValue(FileEvent.EncryptionKeyRequired(space))
            return false
        }

        _currentDirectory.value = null
        _selectedSpace.value = space
        updateCurrentFiles()

        return true
    }

    fun onDirectoryChange(newFolder: VNFile?) {
        Log.d("Vault", "Change")
        if (_selectedSpace.value == null) return
        if (_currentDirectory.value == null && newFolder == null) return

        val currentDir = _currentDirectory.value
        if (currentDir != null) {
            if (newFolder == null && currentDir.parent != null) {
                _currentDirectory.value = currentDir.parent
            }
        }

        if (newFolder != null) {
            _currentDirectory.value = newFolder
        }

        updateCurrentFiles()
    }

    fun onSearchQuery(query: String?) {
        if (query == null || query.isEmpty()) {
            updateCurrentFiles()
            return
        }
        if (_currentDirectory.value != null) {
            val list = mutableListOf<VNFile>()
            buildSearchList(query.toLowerCase(), _currentDirectory.value!!, list)

            _shownElements.value = list
        }
    }

    private fun buildSearchList(query: String, file: VNFile, list: MutableList<VNFile>) {
        if (file.isFolder) {
            file.content?.forEach {
                Log.e("Vault", it.name)

                if (it.name.toLowerCase().contains(query)) {
                    list.add(it)
                }

                if (it.isFolder) buildSearchList(query, it, list)
            }
        }
    }

    fun onWorkerInfoChange() {
        viewModelScope.launch {
            updateCurrentFiles()
        }
    }
}