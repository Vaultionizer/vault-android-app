package com.vaultionizer.vaultapp.repository.impl

import android.util.Log
import com.vaultionizer.vaultapp.data.model.rest.misc.NetworkVersion
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.repository.MiscRepository
import com.vaultionizer.vaultapp.service.MiscService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject

class MiscRepositoryImpl @Inject constructor(val retrofit: Retrofit) : MiscRepository {

    override suspend fun pingHost(host: String): Flow<Resource<NetworkVersion>> {

        return flow {
            val temp =
                retrofit.newBuilder().baseUrl("https://$host/").client(OkHttpClient()).build()
            Log.e("Vault", temp.baseUrl().toString())

            val response = temp.create(MiscService::class.java).getVersionInfo()

            when (response::class) {
                ApiResult.NetworkError::class -> emit(Resource.NetworkError((response as ApiResult.NetworkError).exception))
                ApiResult.Error::class -> emit(
                    Resource.MiscError.HostServerError(
                        statusCode = (response as ApiResult.Error).statusCode
                    )
                )
                else -> emit(Resource.Success((response as ApiResult.Success).data))
            }
        }.flowOn(Dispatchers.IO)
    }

}