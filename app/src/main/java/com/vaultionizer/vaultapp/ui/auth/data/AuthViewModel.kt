package com.vaultionizer.vaultapp.ui.auth.data

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.repository.MiscRepository
import com.vaultionizer.vaultapp.ui.auth.login.LoginResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel @ViewModelInject constructor(
    val miscRepository: MiscRepository,
    val authRepository: AuthRepository,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _hostFormState = MutableLiveData<HostFormState>()
    val hostFormState: LiveData<HostFormState> = _hostFormState

    private val _userDataFormState = MutableLiveData<UserDataFormState>()
    val userDataFormState: LiveData<UserDataFormState> = _userDataFormState

    private val _hostValidationResult = MutableLiveData<HostValidationResult>()
    val hostValidationResult: LiveData<HostValidationResult> = _hostValidationResult

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val authenticationFormData = AuthFormData()

    fun validateHost(host: String) {
        viewModelScope.launch {
            val ping = miscRepository.pingHost(host).first()

            if(ping is ManagedResult.MiscError.HostServerError) {
                _hostFormState.value = HostFormState(hostError = applicationContext.resources.getString(R.string.host_error_code, ping.statusCode))
                _hostValidationResult.value = HostValidationResult(null)
            }

            if(ping is ManagedResult.Success) {
                _hostFormState.value = HostFormState(hostValid = true)
                _hostValidationResult.value = HostValidationResult(ping.data)
            }

            if(ping is ManagedResult.NetworkError) {
                _hostFormState.value = HostFormState(hostError = applicationContext.resources.getString(R.string.host_error_network))
                _hostValidationResult.value = HostValidationResult(null)
            }
        }
    }

    fun registerWithFormData() {
        viewModelScope.launch {
            val result = authRepository.register(authenticationFormData.host, authenticationFormData.username, authenticationFormData.password, authenticationFormData.authKey)

            result.collect {
                when(it) {
                    is ManagedResult.Success -> {
                        Log.e("Vault", "HELLO ${it.data.localUser.userId} with ${it.data.sessionToken}")
                    }
                    is ManagedResult.UserError.UsernameAlreadyInUseError -> {
                        Log.e("Vault", "Username already in use!")
                    }
                }
            }
        }
    }

    fun loginWithFormData() {
        viewModelScope.launch {
            val result = authRepository.login(authenticationFormData.host, authenticationFormData.username, authenticationFormData.password)

            result.collect {
                when(it) {
                    is ManagedResult.Success -> {
                        _loginResult.value = LoginResult(null)
                    }
                    is ManagedResult.Error -> {
                        _loginResult.value = LoginResult(error = applicationContext.getString(R.string.host_error_code, it.statusCode))
                    }
                }
            }
        }
    }

    fun hostDataChanged(host: String) {
        if(!isHostSyntaxValid(host)) {
            _hostFormState.value = HostFormState(hostError = applicationContext.resources.getString(R.string.host_error_syntax))
        } else {
            authenticationFormData.host = host
            _hostFormState.value = HostFormState(hostError = null, hostValid = true)
        }
    }

    fun userDataChanged(username: String? = null, password: String? = null) {
        if(username != null) {
            authenticationFormData.username = username
        }

        if(password != null) {
            authenticationFormData.password = password
        }

        var usernameError: Int? = null
        var passwordError: Int? = null

        if(username?.length?.compareTo(5) == -1) {
            usernameError = R.string.username_error_length
        }

        _userDataFormState.value = UserDataFormState(
            usernameError = usernameError,
            passwordError = passwordError,
            isDataValid = usernameError == null && passwordError == null
        )
    }

    fun authKeyDataChanged(authKey: String) {
        authenticationFormData.authKey = authKey
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