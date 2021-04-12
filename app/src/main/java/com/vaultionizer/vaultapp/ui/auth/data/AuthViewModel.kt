package com.vaultionizer.vaultapp.ui.auth.data

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.repository.MiscRepository
import com.vaultionizer.vaultapp.ui.auth.login.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
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

    fun resetState() {
        _loginResult.value = null
        _hostValidationResult.value = null
        _userDataFormState.value = UserDataFormState()
        _hostFormState.value = HostFormState()
    }

    fun validateHost(host: String) {
        if (host.isEmpty()) {
            _hostValidationResult.value = HostValidationResult(null)
            _hostFormState.value = HostFormState(null, false)
            return
        }
        viewModelScope.launch {
            val ping = miscRepository.pingHost(host).first()

            if (ping is ManagedResult.MiscError.HostServerError) {
                _hostFormState.value = HostFormState(
                    hostError = applicationContext.resources.getString(
                        R.string.host_error_code,
                        ping.statusCode
                    )
                )
                _hostValidationResult.value = HostValidationResult(null)
            }

            if (ping is ManagedResult.Success) {
                _hostFormState.value = HostFormState(hostValid = true)
                _hostValidationResult.value = HostValidationResult(ping.data)
            }

            if (ping is ManagedResult.NetworkError) {
                _hostFormState.value =
                    HostFormState(hostError = applicationContext.resources.getString(R.string.host_error_network))
                _hostValidationResult.value = HostValidationResult(null)
            }
        }
    }

    fun registerWithFormData() {
        viewModelScope.launch {
            val result = authRepository.register(
                authenticationFormData.host,
                authenticationFormData.username,
                authenticationFormData.password,
                authenticationFormData.authKey
            )

            result.collect {
                when (it) {
                    is ManagedResult.Success -> {
                        Log.e(
                            "Vault",
                            "HELLO ${it.data.localUser.userId} with ${it.data.sessionToken}"
                        )
                        _loginResult.value = LoginResult(null)
                    }
                    is ManagedResult.UserError.UsernameAlreadyInUseError -> {
                        _loginResult.value = LoginResult("Username is already in use!")
                    }
                    else -> {
                        _loginResult.value = LoginResult("An unexpected error occurred!")
                    }
                }
            }
        }
    }

    fun loginWithFormData() {
        viewModelScope.launch {
            Log.e("Vauklt", "Login with ${authenticationFormData.host}")
            val result = authRepository.login(
                authenticationFormData.host,
                authenticationFormData.username,
                authenticationFormData.password
            )

            result.collect {
                Log.d("Vault", it.javaClass.toString())
                when (it) {
                    is ManagedResult.Success -> {
                        _loginResult.value = LoginResult(null)
                    }
                    is ManagedResult.Error -> {
                        _loginResult.value = LoginResult("Invalid credentials!")
                    }
                    is ManagedResult.NetworkError -> {
                        Log.d("Vault", it.exception.localizedMessage)
                    }
                }
            }
        }
    }

    fun hostDataChanged(host: String) {
        if (!isHostSyntaxValid(host)) {
            Log.e("Vault", "Invalid host")
            _hostFormState.value =
                HostFormState(hostError = applicationContext.resources.getString(R.string.host_error_syntax))
        } else {
            Log.e("Vault", "Change host to $host")
            authenticationFormData.host = host
            _hostFormState.value = HostFormState(hostError = null, hostValid = true)
        }
    }

    fun userDataChanged(username: String? = null, password: String? = null) {
        if (username != null) {
            authenticationFormData.username = username
        }

        if (password != null) {
            authenticationFormData.password = password
        }

        var usernameError: Int? = null
        var passwordError: Int? = null

        if (authenticationFormData.username?.length?.compareTo(5) == -1 && !authenticationFormData.username?.isEmpty()) {
            usernameError = R.string.username_error_length
            Log.e("Vault", "Username too short!")
        }

        if (authenticationFormData.password?.length?.compareTo(6) == -1 && !authenticationFormData.password?.isEmpty()) {
            passwordError = R.string.invalid_password
            Log.e("Vault", "Password too short!")
        }

        password.let {
            Log.e("Vault", "Password ${password?.length}")
        }

        _userDataFormState.value = UserDataFormState(
            usernameError = usernameError,
            passwordError = passwordError,
            isDataValid = usernameError == null && passwordError == null && authenticationFormData.username != null && authenticationFormData.password != null
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