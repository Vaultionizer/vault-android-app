package com.vaultionizer.vaultapp.repository.impl

import com.vaultionizer.vaultapp.data.model.rest.misc.NetworkVersion
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.NetworkBoundResource
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.repository.MiscRepository
import com.vaultionizer.vaultapp.service.MiscService
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject

class MiscRepositoryImpl @Inject constructor(val retrofit: Retrofit) : MiscRepository {

    override suspend fun pingHost(host: String): Flow<Resource<NetworkVersion>> {
        return object : NetworkBoundResource<NetworkVersion, NetworkVersion>() {
            override fun shouldFetch(): Boolean = true

            override suspend fun fromDb(): Resource<NetworkVersion> {
                throw RuntimeException("Not reachable.")
            }

            override suspend fun saveToDb(networkResult: NetworkVersion) {}

            override suspend fun fromNetwork(): ApiResult<NetworkVersion> {
                val tempClient =
                    retrofit.newBuilder().baseUrl("https://$host/").client(OkHttpClient()).build()
                return tempClient.create(MiscService::class.java).getVersionInfo()
            }

            override fun dispatchError(result: ApiResult.Error): Resource<NetworkVersion> {
                return Resource.MiscError.HostServerError(result.statusCode)
            }

        }.asFlow()
    }

}