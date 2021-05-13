package com.vaultionizer.vaultapp.data.model.rest.result

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

abstract class NetworkBoundResource<ResultType : Any, RequestType : Any> {
    abstract fun shouldFetch(): Boolean
    abstract suspend fun fromDb(): Resource<ResultType>
    abstract suspend fun saveToDb(networkResult: RequestType)
    abstract suspend fun fromNetwork(): ApiResult<RequestType>

    open fun dispatchError(result: ApiResult.Error): Resource<ResultType> =
        Resource.Error(result.statusCode)

    open fun transformOnSuccess(apiResult: RequestType): ResultType = apiResult as ResultType

    fun asFlow(): Flow<Resource<ResultType>> {
        return flow {
            emit(Resource.Loading(null))

            if (!shouldFetch()) {
                emit(fromDb())
                return@flow
            }

            try {
                val apiResult = fromNetwork()
                if (apiResult !is ApiResult.Success) {
                    if (apiResult is ApiResult.NetworkError) {
                        emit(Resource.NetworkError(apiResult.exception))
                        return@flow
                    }

                    emit(dispatchError(apiResult as ApiResult.Error))
                    return@flow
                }

                saveToDb(apiResult.data)
                emit(Resource.Success(transformOnSuccess(apiResult.data)))
            } catch (ex: Exception) {
                Log.e("Vault", "Error while executing flow: ", ex)
                emit(Resource.NetworkError(ex))
            }
        }.flowOn(Dispatchers.IO)
    }
}