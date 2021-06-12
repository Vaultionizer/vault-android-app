package com.vaultionizer.vaultapp.ui.viewmodel

import android.util.Log
import androidx.core.text.trimmedLength
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.cryptography.CryptoUtils
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.util.qr.CRC32Handler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinSpaceResult(
    val success: Boolean,
    val started: Boolean,
    val done: Boolean,
    val resultText: Int?
)

data class JoinSpaceInputState(
    val wellFormed: Boolean,
    val statusText: Int?
)

@HiltViewModel
class JoinSpaceViewModel @Inject constructor(
    val spaceRepository: SpaceRepository
) : ViewModel() {
    private val _doneTestingJoinSpace = MutableLiveData<JoinSpaceResult>()
    val doneTestingJoinSpace: LiveData<JoinSpaceResult> = _doneTestingJoinSpace

    private val _joinSpaceInputState = MutableLiveData<JoinSpaceInputState>()
    val joinSpaceInputState: LiveData<JoinSpaceInputState> = _joinSpaceInputState


    fun reset() {
        _doneTestingJoinSpace.value = JoinSpaceResult(false, false, false, null)
        _joinSpaceInputState.value = JoinSpaceInputState(true, null)
    }

    fun joinSpace(payload: String, password: String) {
        _doneTestingJoinSpace.value = JoinSpaceResult(false, true, false, null)
        viewModelScope.launch {
            val pair = CRC32Handler.parseContent(payload) ?: return@launch
            val spaceId = spaceRepository.peekNextSpaceId()
            val success = CryptoUtils.importKeyForSharedSpace(spaceId, pair.key, password)
            if (!success) {
                _doneTestingJoinSpace.value = JoinSpaceResult(false, false, true, null)
                return@launch
            }

            spaceRepository.joinSpace(pair.remoteSpaceId, spaceId, pair.authKey).collect {
                when(it){
                    is Resource.Success -> {
                        _doneTestingJoinSpace.value = JoinSpaceResult(true, false, true, null)
                    }
                    else -> {
                        _doneTestingJoinSpace.value = JoinSpaceResult(false, false, true, null)
                        // TODO handle error
                    }
                }
            }


        }
    }

    fun checkWellFormed(content: String) {
        var wellFormed: Boolean
        if (content.trimmedLength() == 0) {
            _joinSpaceInputState.value = JoinSpaceInputState(false, null)
            return
        } else if (content.length < 3 || content.count { c ->
                return@count c == '@'
            } != 2) {
            wellFormed = false
        } else {
            val parts = content.split("@")
            wellFormed =
                !(parts.size != 3 || stringInvalid(parts[0]) || stringInvalid(parts[1]) || stringInvalid(
                    parts[2]
                ))
        }
        _joinSpaceInputState.value = JoinSpaceInputState(
            wellFormed,
            if (wellFormed) null else R.string.join_space_input_state
        )

    }

    private fun stringInvalid(content: String): Boolean {
        return (content.isNullOrBlank() || content.isEmpty())
    }
}