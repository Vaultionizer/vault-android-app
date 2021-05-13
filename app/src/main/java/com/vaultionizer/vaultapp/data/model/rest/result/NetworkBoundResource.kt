package com.vaultionizer.vaultapp.data.model.rest.result

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

abstract class NetworkBoundResource<ResultType : Any, RequestType : Any> {
    abstract fun shouldFetch(): Boolean
    abstract suspend fun fromDb(): Resource<ResultType>
    abstract suspend fun saveToDb(networkResult: RequestType)
    abstract suspend fun fromNetwork(): ApiResult<RequestType>
    abstract fun dispatchError(result: ApiResult<RequestType>): Resource<ResultType>

    open fun initialLoadingValue(): ResultType? = null
    open fun transformOnSuccess(apiResult: RequestType): ResultType = apiResult as ResultType

    fun asFlow(): Flow<Resource<ResultType>> {
        return flow {
            emit(Resource.Loading(initialLoadingValue()))

            if (!shouldFetch()) {
                return@flow
            }

            try {
                val apiResult = fromNetwork()
                if (apiResult !is ApiResult.Success) {
                    emit(dispatchError(apiResult))
                    return@flow
                }

                saveToDb(apiResult.data)
                emit(Resource.Success(transformOnSuccess(apiResult.data)))
            } catch (ex: Exception) {
                emit(Resource.NetworkError(ex))
            }
        }.flowOn(Dispatchers.IO)
    }
}