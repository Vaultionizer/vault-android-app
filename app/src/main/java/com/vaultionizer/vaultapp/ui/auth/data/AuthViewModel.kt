package com.vaultionizer.vaultapp.ui.auth.data

import android.content.Context
import android.util.Patterns
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.misc.Version
import com.vaultionizer.vaultapp.data.source.MiscService
import com.vaultionizer.vaultapp.hilt.RestModule
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel @ViewModelInject constructor(
        val miscService: MiscService,
        @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _hostFormState = MutableLiveData<HostFormState>()
    val hostFormState: LiveData<HostFormState> = _hostFormState

    private val _hostValidationResult = MutableLiveData<HostValidationResult>()
    val hostValidationResult: LiveData<HostValidationResult> = _hostValidationResult

    fun validateHost(host: String) {
        var host = host
        if(!host.startsWith("http")) {
            host = "https://$host"
        }

        RestModule.host = host

        miscService.getVersionInfo().enqueue(object : Callback<Version> {
            override fun onResponse(call: Call<Version>, response: Response<Version>) {
                if(response.isSuccessful && response.body() != null) {
                    _hostFormState.value = HostFormState(hostValid = true)
                    _hostValidationResult.value = HostValidationResult(response.body()!!)
                } else {
                    _hostFormState.value = HostFormState(hostError = applicationContext.resources.getString(R.string.host_error_code, response.code()))
                    _hostValidationResult.value = HostValidationResult(null)
                }
            }

            override fun onFailure(call: Call<Version>, t: Throwable) {
                _hostFormState.value = HostFormState(hostError = applicationContext.resources.getString(R.string.host_error_network))
                _hostValidationResult.value = HostValidationResult(null)
            }

        })
    }

    fun hostDataChanged(host: String) {
        if(!isHostSyntaxValid(host)) {
            _hostFormState.value = HostFormState(hostError = applicationContext.resources.getString(R.string.host_error_syntax))
        } else {
            _hostFormState.value = HostFormState(hostError = null, hostValid = true)
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        TODO()
        return false
    }

    private fun isHostSyntaxValid(host: String): Boolean {
        return Patterns.WEB_URL.matcher(host).matches()
    }

    companion object {
        const val MIN_API_VERSION = 0.1
    }

}