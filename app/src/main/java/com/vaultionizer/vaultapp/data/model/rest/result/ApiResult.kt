package com.vaultionizer.vaultapp.data.model.rest.result

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class ApiResult<out T : Any> {

    /**
     * Shared errors
     */
    data class Success<out T : Any>(val data: T) : ApiResult<T>()
    data class Error(val statusCode: Int) : ApiResult<Nothing>()
    data class NetworkError(val exception: Throwable) : ApiResult<Nothing>()

}