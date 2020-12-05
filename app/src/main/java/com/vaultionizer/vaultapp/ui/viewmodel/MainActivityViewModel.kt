package com.vaultionizer.vaultapp.ui.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.repository.FileRepository
import com.vaultionizer.vaultapp.repository.SpaceRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*


class MainActivityViewModel @ViewModelInject constructor(
    val spaceRepository: SpaceRepository,
    val fileRepository: FileRepository
): ViewModel() {

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
                return@launch
            }

            if(_currentDirectory.value != null) {
                _shownElements.value = _currentDirectory.value!!.content
            } else {
                val response = fileRepository.getFileTree(_selectedSpace.value!!)

                response.collect {
                    when(it) {
                        is ManagedResult.Success -> {
                            _currentDirectory.value = it.data
                            updateCurrentFiles()
                        }
                    }
                }
            }
        }
    }

    fun requestUpload(uri: Uri, context: Context) {
        viewModelScope.launch {
            val resolver = context.contentResolver
            resolver.openInputStream(uri)?.use {
                val content = it.readBytes()

                fileRepository.uploadFile(selectedSpace.value!!, _currentDirectory.value!!, content, getFileName(uri, resolver) ?: "?? Unknown ??", context).collect {
                    updateCurrentFiles()
                }
            }
        }
    }

    fun selectedSpaceChanged(space: VNSpace) {
        Log.e("Vault", "Change space...")
        _selectedSpace.value = space
        updateCurrentFiles()
    }

    fun requestNewSpace() {

    }

    fun onDirectoryChange(newFolder: VNFile?) {
        Log.d("Vault", "Change")
        if(_selectedSpace.value == null) return
        if(_currentDirectory.value == null && newFolder == null) return

        val currentDir = _currentDirectory.value
        if(currentDir != null) {
            if(newFolder == null && currentDir.parent != null) {
                _currentDirectory.value = currentDir.parent
            }
        }

        if(newFolder != null) {
            _currentDirectory.value = newFolder
        }

        updateCurrentFiles()
    }

    fun onSearchQuery(query: String) {

    }

    // https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content
    private fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

}