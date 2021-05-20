package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.model.rest.misc.NetworkVersion
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import kotlinx.coroutines.flow.Flow

interface MiscRepository {

    /**
     * Pings a host and returns information about the maintainer.
     *
     * @see [com.vaultionizer.vaultapp.service.MiscService.getVersionInfo]
     * @see [com.vaultionizer.vaultapp.data.model.rest.result.NetworkBoundResource]
     *
     * @param host  Hostname.
     * @return      A flow as described for [com.vaultionizer.vaultapp.data.model.rest.result.NetworkBoundResource].
     */
    suspend fun pingHost(host: String): Flow<Resource<NetworkVersion>>
}