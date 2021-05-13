package com.vaultionizer.vaultapp.data.model.rest.result

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

abstract class NetworkBoundResource<ResultType : Any> {
    abstract fun shouldFetch(): Boolean
    abstract fun fromDb(): ResultType
    abstract fun saveToDb(networkResult: ResultType?)
    abstract fun fromNetwork(): ApiResult<ResultType>
    abstract fun dispatchError(result: ApiResult<ResultType>): Resource<ResultType>

    open fun initialLoadingValue(): ResultType? = null

    fun asFlow(): Flow<Resource<ResultType>> {
        return flow {
            emit(Resource.Loading(initialLoadingValue()))

            if (!shouldFetch()) {
                emit(Resource.Success(fromDb()))
                return@flow
            }

            try {
                val apiResult = fromNetwork()
                if (apiResult !is ApiResult.Success) {
                    emit(dispatchError(apiResult))
                    return@flow
                }

                saveToDb(apiResult.data)
                emit(Resource.Success(apiResult.data))
            } catch (ex: Exception) {
                emit(Resource.NetworkError(ex))
            }
        }.flowOn(Dispatchers.IO)
    }
}