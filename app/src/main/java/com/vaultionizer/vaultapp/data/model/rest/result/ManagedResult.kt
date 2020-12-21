package com.vaultionizer.vaultapp.data.model.rest.result

sealed class ManagedResult<out T : Any> {

    /**
     * General
     */
    data class Success<out T : Any>(val data: T) : ManagedResult<T>()
    data class Error(val statusCode: Int) : ManagedResult<Nothing>()
    data class NetworkError(val exception: Throwable) : ManagedResult<Nothing>()
    object ConsistencyError : ManagedResult<Nothing>()

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

    sealed class RefFileError<out T : Any> : ManagedResult<T>() {
        object RefFileChecksumError : RefFileError<Nothing>()
        object RefFileDownloadError : RefFileError<Nothing>()
        object RefFileUploadError : RefFileError<Nothing>()
    }

    sealed class ExchangeError<out T : Any> : ManagedResult<T>() {
        object FileDeletionError : ExchangeError<Nothing>()
    }
}