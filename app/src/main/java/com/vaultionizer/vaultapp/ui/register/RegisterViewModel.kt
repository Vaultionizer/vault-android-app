package com.vaultionizer.vaultapp.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.vaultionizer.vaultapp.data.LoginRepository
import com.vaultionizer.vaultapp.data.Result

import com.vaultionizer.vaultapp.R

class RegisterViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<RegisterFormState>()
    val loginFormState: LiveData<RegisterFormState> = _loginForm

    private val _loginResult = MutableLiveData<RegisterResult>()
    val loginResult: LiveData<RegisterResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        if (result is Result.Success) {
            _loginResult.value = RegisterResult(success = RegisteredUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = RegisterResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = RegisterFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = RegisterFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}