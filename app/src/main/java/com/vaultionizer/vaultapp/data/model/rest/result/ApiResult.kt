package com.vaultionizer.vaultapp.data.model.rest.result

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class ApiResult<out T : Any> {

    /**
     * Shared States
     */
    data class Success<out T : Any>(val data: T) : ApiResult<T>()
    data class Error(val statusCode: Int) : ApiResult<Nothing>()
    data class NetworkError(val exception: Throwable) : ApiResult<Nothing>()

    fun mapToResource(): Resource<T> = when (this) {
        is Success -> Resource.Success(data)
        is Error -> Resource.Error(statusCode)
        is NetworkError -> Resource.NetworkError(exception)
    }

    fun <M : Any> mapToResource(successData: M): Resource<M> = when (this) {
        is Success -> Resource.Success(successData)
        is Error -> Resource.Error(statusCode)
        is NetworkError -> Resource.NetworkError(exception)
    }

}