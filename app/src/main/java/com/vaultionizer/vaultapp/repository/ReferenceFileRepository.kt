package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import kotlinx.coroutines.flow.Flow

interface ReferenceFileRepository {

    suspend fun downloadReferenceFile(space: VNSpace): Flow<Resource<NetworkReferenceFile>>

    suspend fun uploadReferenceFile(
        referenceFile: NetworkReferenceFile,
        space: VNSpace
    ): Flow<Resource<NetworkReferenceFile>>

    suspend fun syncReferenceFile(
        spaceId: Long,
        root: VNFile
    ): Flow<Resource<NetworkReferenceFile>>

}