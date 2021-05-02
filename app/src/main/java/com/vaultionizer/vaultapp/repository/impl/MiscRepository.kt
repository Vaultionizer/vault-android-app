package com.vaultionizer.vaultapp.repository.impl

import android.util.Log
import com.vaultionizer.vaultapp.data.model.rest.misc.NetworkVersion
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.service.MiscService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject

class MiscRepository @Inject constructor(val retrofit: Retrofit) {

    suspend fun pingHost(host: String): Flow<ManagedResult<NetworkVersion>> {

        return flow {
            val temp =
                retrofit.newBuilder().baseUrl("https://$host/").client(OkHttpClient()).build()
            Log.e("Vault", temp.baseUrl().toString())

            val response = temp.create(MiscService::class.java).getVersionInfo()

            when (response::class) {
                ApiResult.NetworkError::class -> emit(ManagedResult.NetworkError((response as ApiResult.NetworkError).exception))
                ApiResult.Error::class -> emit(
                    ManagedResult.MiscError.HostServerError(
                        statusCode = (response as ApiResult.Error).statusCode ?: -1
                    )
                )
                else -> emit(ManagedResult.Success((response as ApiResult.Success).data))
            }
        }.flowOn(Dispatchers.IO)
    }

}