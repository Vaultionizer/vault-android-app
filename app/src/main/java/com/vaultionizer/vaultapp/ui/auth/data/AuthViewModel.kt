package com.vaultionizer.vaultapp.ui.auth.data

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.cryptography.PasswordValidator
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.repository.MiscRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val miscRepository: MiscRepository,
    val authRepository: AuthRepository,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _authenticationEvent = MutableLiveData<AuthEvent>()
    val authenticationEvent: LiveData<AuthEvent> = _authenticationEvent

    private var authenticationFormData = AuthFormData()

    fun resetState() {
        _authenticationEvent.value = null
        authenticationFormData = AuthFormData()
    }

    fun validateHost(host: String) {
        if (host.isEmpty()) {
            _authenticationEvent.postValue(AuthEvent.HostValidation(error = getString(R.string.host_error_syntax)))
            return
        }

        viewModelScope.launch {
            miscRepository.pingHost(formatHost(host)).collect {
                val event = when (it) {
                    is Resource.Loading ->
                        AuthEvent.HostValidation(
                            isLoading = true
                        )

                    is Resource.Success ->
                        AuthEvent.HostValidation(
                            version = it.data
                        )

                    is Resource.MiscError.HostServerError ->
                        AuthEvent.HostValidation(
                            error = getString(R.string.host_error_code, it.statusCode)
                        )

                    else ->
                        AuthEvent.HostValidation(
                            error = getString(R.string.all_unexpected_error)
                        )
                }

                _authenticationEvent.postValue(event)
            }
        }
    }

    private fun formatHost(host: String): String {
        var formattedHost: String = host;
        // remove protocol if existing and remove potential last "/"
        if (formattedHost[host.length-1] == '/') {
            formattedHost = formattedHost.dropLast(1);
        }
        if (host.contains("://")) {
            formattedHost = formattedHost.split("://")[1]
        }
        return formattedHost
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
                val event = when (it) {
                    is Resource.Loading ->
                        AuthEvent.LoginValidation(
                            isLoading = true
                        )

                    is Resource.Success ->
                        AuthEvent.LoginValidation()

                    is Resource.UserError.UsernameAlreadyInUseError ->
                        AuthEvent.LoginValidation(
                            error = getString(R.string.register_error_username_taken)
                        )

                    else ->
                        AuthEvent.LoginValidation(
                            error = getString(R.string.all_unexpected_error)
                        )
                }

                _authenticationEvent.postValue(event)
            }
        }
    }

    fun loginWithFormData() {
        viewModelScope.launch {
            Log.e("Vault", "Login with ${authenticationFormData.host}")
            val result = authRepository.login(
                authenticationFormData.host,
                authenticationFormData.username,
                authenticationFormData.password
            )

            result.collect {
                val event = when (it) {
                    is Resource.Loading ->
                        AuthEvent.LoginValidation(
                            isLoading = true
                        )

                    is Resource.Success ->
                        AuthEvent.LoginValidation()

                    else ->
                        // TODO(jatsqi): Refactor error dispatcher
                        AuthEvent.LoginValidation(
                            error = getString(R.string.login_error_invalid_credentials)
                        )
                }

                _authenticationEvent.postValue(event)
            }
        }
    }

    fun hostDataChanged(host: String) {
        if (!isHostSyntaxValid(host)) {
            Log.e("Vault", "Invalid host")
            _authenticationEvent.postValue(
                AuthEvent.HostValidation(
                    error = getString(R.string.host_error_syntax)
                )
            )
        } else {
            Log.e("Vault", "Change host to $host")
            authenticationFormData.host = host
            _authenticationEvent.postValue(AuthEvent.HostValidation())
        }
    }

    fun userDataChanged(username: String? = null, password: String? = null) {
        if (username != null) {
            authenticationFormData.username = username
        }

        if (password != null) {
            authenticationFormData.password = password
        }

        Log.e("Vault", "Password valid? "+PasswordValidator().validatePassword(authenticationFormData.password).text)
        var usernameError: Int? = null
        var passwordError: Int? = null

        if (authenticationFormData.username?.length?.compareTo(5) == -1 && !authenticationFormData.username?.isEmpty()) {
            usernameError = R.string.username_error_length
            Log.e("Vault", "Username too short!")
        }


        var validPwd = PasswordValidator().validatePassword(authenticationFormData.password)
        if (validPwd.error && usernameError == null) {
            passwordError = validPwd.text
            Log.e("Vault", "Password invalid: " + validPwd.name)
        }

        password.let {
            Log.e("Vault", "Password ${password?.length}")
        }

        _authenticationEvent.postValue(
            AuthEvent.UserDataValidation(
                usernameError = getString(usernameError),
                passwordError = getString(passwordError),
                isDataValid = usernameError == null && passwordError == null
            )
        )
    }

    fun authKeyDataChanged(authKey: String) {
        authenticationFormData.authKey = authKey
    }

    private fun isHostSyntaxValid(host: String): Boolean {
        return Patterns.WEB_URL.matcher(host).matches()
    }

    private fun getString(res: Int?, vararg arguments: Any): String? {
        if (res == null) {
            return null
        }

        Log.e("Vault", "RES $res")
        return applicationContext.resources.getString(res, *arguments)
    }

}