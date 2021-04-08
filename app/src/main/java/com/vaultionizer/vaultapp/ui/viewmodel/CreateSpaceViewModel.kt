package com.vaultionizer.vaultapp.ui.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.cryptography.Cryptography
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.ui.main.space.SpaceCreationResult
import com.vaultionizer.vaultapp.ui.main.space.SpaceFormState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CreateSpaceViewModel @ViewModelInject constructor(val spaceRepository: SpaceRepository) :
    ViewModel() {

    private val _spaceCreationResult = MutableLiveData<SpaceCreationResult>()
    val spaceCreationResult: LiveData<SpaceCreationResult> = _spaceCreationResult

    private val _spaceFormState = MutableLiveData<SpaceFormState>()
    val spaceFormState: LiveData<SpaceFormState> = _spaceFormState

    private var spaceNameFormData: String? = null

    fun createSpace(name: String, isPrivate: Boolean, algorithm: String) {
        viewModelScope.launch {
            spaceRepository.createSpace(name, isPrivate).collect {
                when (it) {
                    is ManagedResult.Success -> {
                        _spaceCreationResult.value = SpaceCreationResult(it.data, true)

                        Cryptography().createKey(
                            it.data.id,
                            CryptoType.AES,
                            CryptoMode.GCM,
                            CryptoPadding.NONE
                        )
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