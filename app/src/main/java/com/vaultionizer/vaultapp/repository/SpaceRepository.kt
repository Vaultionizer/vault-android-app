package com.vaultionizer.vaultapp.repository

import com.google.gson.Gson
import com.thedeanda.lorem.LoremIpsum
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.entity.LocalSpace
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.CreateSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.data.model.rest.space.NetworkSpace
import com.vaultionizer.vaultapp.service.SpaceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class SpaceRepository @Inject constructor(
    val spaceService: SpaceService,
    val localSpaceDao: LocalSpaceDao,
    val localFileDao: LocalFileDao,
    val gson: Gson
) {
    suspend fun getAllSpaces(): Flow<ManagedResult<List<VNSpace>>> {
        return flow {
            val response = spaceService.getAll()

            when (response) {
                is ApiResult.Success -> {
                    val list = mutableListOf<VNSpace>()
                    for (space in response.data) {
                        list.add(persistNetworkSpace(space))
                    }

                    emit(ManagedResult.Success(list))
                }
                // TODO(jatsqi): Error handling
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSpace(spaceId: Long): Flow<ManagedResult<VNSpace>> {
        return flow {
            getAllSpaces().collect {
                when(it) {
                    is ManagedResult.Success -> {
                        val space = localSpaceDao.getSpaceById(spaceId)
                        if(space == null) {
                            emit(ManagedResult.Error(404))
                        } else {
                            emit(ManagedResult.Success(it.data.first { it.id == spaceId }))
                        }
                    }
                    // TODO(jatsqi): Error handling
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun createSpace(name: String, isPrivate: Boolean): Flow<ManagedResult<VNSpace>> {
        return flow {
            // TODO(jatsqi) Replace LoremIpsum with real authKey
            val response = spaceService.createSpace(
                CreateSpaceRequest(
                    LoremIpsum.getInstance().getWords(20),
                    isPrivate,
                    gson.toJson(NetworkReferenceFile.EMPTY_FILE)
                )
            )

            when (response) {
                is ApiResult.Success -> {
                    val persisted = persistNetworkSpace(
                        response.data,
                        isPrivate,
                        name,
                        null
                    ) // TODO(jatsqi) Replace null with actual reference file

                    emit(ManagedResult.Success(persisted))
                }
                // TODO(jatsqi): Error handling
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteSpace(space: VNSpace): Flow<ManagedResult<VNSpace>> {
        return flow {

            when (spaceService.deleteSpace(space.remoteId)) {
                is ApiResult.Success -> {
                    localFileDao.deleteFilesBySpace(space.id)
                    localSpaceDao.deleteSpaces(localSpaceDao.getSpaceById(space.id)!!)

                    emit(ManagedResult.Success(space))
                }
                // TODO(jatsqi): Error handling
            }
        }
    }

    private fun persistNetworkSpace(
        remoteSpaceId: Long,
        isPrivate: Boolean,
        name: String?,
        refFile: String?
    ): VNSpace {
        var space =
            localSpaceDao.getSpaceByRemoteId(AuthRepository.user!!.localUser.userId, remoteSpaceId)
        if (space == null) {
            space = LocalSpace(
                0,
                remoteSpaceId,
                AuthRepository.user!!.localUser.userId,
                name,
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
            isPrivate
        )
    }

    private fun persistNetworkSpace(networkSpace: NetworkSpace): VNSpace =
        persistNetworkSpace(networkSpace.spaceID, networkSpace.private, null, null)
}