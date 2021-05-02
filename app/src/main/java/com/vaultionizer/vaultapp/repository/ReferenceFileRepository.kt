package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import kotlinx.coroutines.flow.Flow

interface ReferenceFileRepository {

    suspend fun downloadReferenceFile(space: VNSpace): Flow<ManagedResult<NetworkReferenceFile>>

    suspend fun uploadReferenceFile(
        referenceFile: NetworkReferenceFile,
        space: VNSpace
    ): Flow<ManagedResult<NetworkReferenceFile>>

    suspend fun syncReferenceFile(
        spaceId: Long,
        root: VNFile
    ): Flow<ManagedResult<NetworkReferenceFile>>

}