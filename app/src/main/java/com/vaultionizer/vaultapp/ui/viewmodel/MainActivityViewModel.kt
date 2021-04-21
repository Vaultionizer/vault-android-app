package com.vaultionizer.vaultapp.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.ui.main.file.FileDialogState
import com.vaultionizer.vaultapp.util.Constants
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

    private val _fileDialogState = MutableLiveData<FileDialogState>()
    val fileDialogState: LiveData<FileDialogState> = _fileDialogState

    val fileWorkerInfo: LiveData<List<WorkInfo>> =
        WorkManager.getInstance(context).getWorkInfosByTagLiveData(Constants.WORKER_TAG_FILE)

    fun updateUserSpaces() {
        viewModelScope.launch {
            val result = spaceRepository.getAllSpaces()

            result.collect {
                if (it is ManagedResult.Success) {
                    _userSpaces.value = it.data
                }
            }
        }
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
                        is ManagedResult.Success -> {
                            _currentDirectory.value = it.data
                            updateCurrentFiles()
                        }
                    }
                }
            }
        }
    }

    fun requestUpload(uri: Uri) {
        viewModelScope.launch {
            fileRepository.uploadFile(
                selectedSpace.value!!,
                uri,
                _currentDirectory.value!!,
            )
            _fileDialogState.value = FileDialogState(isValid = true)
        }
    }

    fun requestDownload(file: VNFile) {
        viewModelScope.launch {
            fileRepository.downloadFile(file)
        }
    }

    fun requestFolder(name: String) {
        if (_selectedSpace.value != null && _currentDirectory.value != null) {
            viewModelScope.launch {
                fileRepository.uploadFolder(_selectedSpace.value!!, name, _currentDirectory.value!!)
                    .collect {
                        when (it) {
                            is ManagedResult.Success -> {
                                updateCurrentFiles()
                                _fileDialogState.value = FileDialogState(isValid = true)
                            }
                            else -> {
                                _fileDialogState.value =
                                    FileDialogState(fileError = R.string.host_error_network)
                            }
                        }
                    }
            }
        }
    }

    fun requestDeletion(file: VNFile) {
        viewModelScope.launch {
            fileRepository.deleteFile(file).collect {
                when (it) {
                    is ManagedResult.Success -> {
                        _fileDialogState.value = FileDialogState(isValid = true)
                        updateCurrentFiles()
                    }
                    else -> { // TODO(jatsqi) Error handling
                        _fileDialogState.value =
                            FileDialogState(fileError = R.string.host_error_network)
                    }
                }
            }
        }
    }

    fun requestSpaceDeletion() {
        if (_userSpaces.value?.size?.minus(1) == 0) return
        viewModelScope.launch {
            spaceRepository.deleteSpace(_selectedSpace.value!!).collect {
                when (it) {
                    is ManagedResult.Success -> {
                        val spaces = _userSpaces.value!!.toMutableList()
                        spaces.remove(it)

                        fileRepository.cacheEvict(it.data.id)
                        _selectedSpace.value = spaces[0]
                        _currentDirectory.value = null
                        updateUserSpaces()
                        updateCurrentFiles()

                        _fileDialogState.value = FileDialogState(isValid = true)
                    }
                }
            }
        }
    }

    fun selectedSpaceChanged(space: VNSpace) {
        Log.e("Vault", "Change space...")
        _currentDirectory.value = null
        _selectedSpace.value = space
        updateCurrentFiles()
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