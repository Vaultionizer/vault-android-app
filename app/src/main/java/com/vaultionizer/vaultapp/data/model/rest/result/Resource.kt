package com.vaultionizer.vaultapp.data.model.rest.result

/**
 * The single purpose of this class is to increase code readability
 */
sealed class Resource<out T : Any> {

    /**
     * General
     */
    data class Success<out T : Any>(val data: T) : Resource<T>()
    data class Error(val statusCode: Int) : Resource<Nothing>()
    data class NetworkError(val exception: Throwable) : Resource<Nothing>()
    data class Loading<out T : Any>(val data: T?) : Resource<T>()
    object ConsistencyError : Resource<Nothing>()

    /**
     * User errors
     */
    sealed class UserError<out T : Any> : Resource<T>() {
        object UsernameAlreadyInUseError : UserError<Nothing>()
        object ValueConstraintsError : UserError<Nothing>()
    }

    sealed class MiscError<out T : Any> : Resource<T>() {
        data class HostServerError(val statusCode: Int) : MiscError<Nothing>()
        data class OutdatedVaultServerError(var foundVersion: String) : MiscError<Nothing>()
    }

    sealed class RefFileError<out T : Any> : Resource<T>() {
        object RefFileChecksumError : RefFileError<Nothing>()
        object RefFileDownloadError : RefFileError<Nothing>()
        object RefFileUploadError : RefFileError<Nothing>()
    }

    sealed class ExchangeError<out T : Any> : Resource<T>() {
        object FileDeletionError : ExchangeError<Nothing>()
    }

    sealed class SpaceError<out T : Any> : Resource<T>() {
        object SpaceNotFoundError : SpaceError<Nothing>()
    }
}