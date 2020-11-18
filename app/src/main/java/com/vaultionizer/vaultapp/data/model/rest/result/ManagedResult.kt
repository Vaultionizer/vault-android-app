package com.vaultionizer.vaultapp.data.model.rest.result

sealed class ManagedResult<out T : Any> {

    /**
     * General
     */
    data class Success<out T : Any>(val data: T) : ManagedResult<T>()
    data class UnknownError(val exception: Throwable) : ManagedResult<Nothing>()
    data class NetworkError(val exception: Throwable) : ManagedResult<Nothing>()
    object PermissionsError : ManagedResult<Nothing>()
    object NotFoundError : ManagedResult<Nothing>()

    /**
     * User errors
     */
    sealed class UserError<out T : Any> : ManagedResult<T>() {
        object UsernameAlreadyInUseError : UserError<Nothing>()
        object ValueConstraintsError : UserError<Nothing>()
    }

    sealed class MiscError<out T : Any> : ManagedResult<T>() {
        data class HostServerError(val statusCode: Int) : MiscError<Nothing>()
        data class OutdatedVaultServerError(var foundVersion: String) : MiscError<Nothing>()
    }

}