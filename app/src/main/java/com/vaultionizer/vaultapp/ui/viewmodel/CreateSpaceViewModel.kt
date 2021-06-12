package com.vaultionizer.vaultapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.ui.main.space.SpaceCreationResult
import com.vaultionizer.vaultapp.ui.main.space.SpaceFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateSpaceViewModel @Inject constructor(val spaceRepository: SpaceRepository) :
    ViewModel() {

    private val _spaceCreationResult = MutableLiveData<SpaceCreationResult>()
    val spaceCreationResult: LiveData<SpaceCreationResult> = _spaceCreationResult

    private val _spaceFormState = MutableLiveData<SpaceFormState>()
    val spaceFormState: LiveData<SpaceFormState> = _spaceFormState

    private var spaceNameFormData: String? = null

    fun createSpace(
        name: String,
        isPrivate: Boolean,
        algorithm: String,
        writeAccess: Boolean,
        authKeyAccess: Boolean,
        password: String?
    ) {
        viewModelScope.launch {
            spaceRepository.createSpace(name, isPrivate, writeAccess, authKeyAccess, algorithm, password)
                .collect {
                    when (it) {
                        is Resource.Success -> {
                            _spaceCreationResult.value = SpaceCreationResult(it.data, true)
                        }
                        else -> {
                            _spaceCreationResult.value = SpaceCreationResult(null, false)
                        }
                    }
                }
        }
    }

    fun spaceNameChanged(name: String) {
        spaceNameFormData = name.trim()

        if (spaceNameFormData?.trim()?.length?.compareTo(4) == -1) {
            if (spaceNameFormData?.isEmpty() == false) {
                _spaceFormState.value = SpaceFormState(R.string.create_space_name_error)
            } else {
                _spaceFormState.value = SpaceFormState(isDataValid = false)
            }
        } else {
            _spaceFormState.value = SpaceFormState(isDataValid = true)
        }
    }

}