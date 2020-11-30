package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.entity.LocalSpace
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.space.NetworkSpace
import com.vaultionizer.vaultapp.service.SpaceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SpaceRepository @Inject constructor(val spaceService: SpaceService, val localSpaceDao: LocalSpaceDao) {

    suspend fun getAllSpaces(): Flow<ManagedResult<List<VNSpace>>> {
        return flow {
            val response = spaceService.getAll()

            when(response) {
                is ApiResult.Success -> {
                    val list = mutableListOf<VNSpace>()
                    for (space in response.data) {
                        list.add(persistNetworkSpace(space))
                    }

                    emit(ManagedResult.Success(list))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun persistNetworkSpace(networkSpace: NetworkSpace): VNSpace {
        var space = localSpaceDao.getSpaceByRemoteId(AuthRepository.user!!.localUser.userId, networkSpace.spaceID)
        if(space == null) {
            space = LocalSpace(
                0,
                networkSpace.spaceID,
                AuthRepository.user!!.localUser.userId,
                null,
                null,
                0
            )

            space.spaceId = localSpaceDao.createSpace(space)
        }

        return VNSpace(
            space.spaceId,
            space.remoteSpaceId,
            space.userId,
            space.name,
            space.lastAccess,
            networkSpace.private
        )
    }
}