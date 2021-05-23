package com.vaultionizer.vaultapp.repository.impl

import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.cache.AuthCache
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.entity.LocalSpace
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.CreateSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.NetworkBoundResource
import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.data.model.rest.space.NetworkSpace
import com.vaultionizer.vaultapp.repository.SpaceRepository
import com.vaultionizer.vaultapp.service.SpaceService
import com.vaultionizer.vaultapp.util.AuthKeyGen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpaceRepositoryImpl @Inject constructor(
    val spaceService: SpaceService,
    val localSpaceDao: LocalSpaceDao,
    val localFileDao: LocalFileDao,
    val gson: Gson,
    val authCache: AuthCache
) : SpaceRepository {

    override suspend fun getAllSpaces(): Flow<Resource<List<VNSpace>>> {
        return object : NetworkBoundResource<List<VNSpace>, List<NetworkSpace>>() {
            override fun shouldFetch(): Boolean = true

            override suspend fun fromDb(): Resource<List<VNSpace>> {
                TODO()
            }

            override suspend fun saveToDb(networkResult: List<NetworkSpace>) {
                for (space in networkResult) {
                    persistNetworkSpace(space)
                }
            }

            override suspend fun fromNetwork(): ApiResult<List<NetworkSpace>> {
                return spaceService.getAll()
            }

            override fun transformOnSuccess(apiResult: List<NetworkSpace>): List<VNSpace> {
                // TODO(jatsqi): Return cached list
                val list = mutableListOf<VNSpace>()
                for (space in apiResult) {
                    runBlocking {
                        list.add(persistNetworkSpace(space))
                    }
                }
                return list
            }

        }.asFlow()
    }

    override suspend fun getSpace(spaceId: Long): Flow<Resource<VNSpace>> {
        return flow {
            getAllSpaces().collect {
                when (it) {
                    is Resource.Success -> {
                        val space = localSpaceDao.getSpaceById(spaceId)
                        if (space == null) {
                            emit(Resource.Error(404))
                        } else {
                            emit(Resource.Success(it.data.first { it.id == spaceId }))
                        }
                    }
                    // TODO(jatsqi): Error handling
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getSpaceRemoteId(spaceId: Long): Long? {
        return withContext(Dispatchers.IO) {
            return@withContext localSpaceDao.getSpaceById(spaceId)?.remoteSpaceId
        }
    }

    override suspend fun peekNextSpaceId(): Long {
        return localSpaceDao.getNextSpaceId()
    }

    override suspend fun createSpace(
        name: String,
        isPrivate: Boolean,
        writeAccess: Boolean,
        authKeyAccess: Boolean
    ): Flow<Resource<VNSpace>> {
        return flow {
            // TODO(jatsqi) Replace LoremIpsum with real authKey
            val response = spaceService.createSpace(
                CreateSpaceRequest(
                    AuthKeyGen().generateAuthKey(),
                    isPrivate,
                    gson.toJson(NetworkReferenceFile.EMPTY_FILE),
                    authKeyAccess,
                    writeAccess
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

                    emit(Resource.Success(persisted))
                }
                // TODO(jatsqi): Error handling
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun deleteSpace(space: VNSpace): Flow<Resource<VNSpace>> {
        return flow {

            when (spaceService.deleteSpace(space.remoteId)) {
                is ApiResult.Success -> {
                    localFileDao.deleteFilesBySpace(space.id)
                    localSpaceDao.deleteSpaces(localSpaceDao.getSpaceById(space.id)!!)

                    emit(Resource.Success(space))
                }
                // TODO(jatsqi): Error handling
            }
        }
    }

    private suspend fun persistNetworkSpace(
        remoteSpaceId: Long,
        isPrivate: Boolean,
        name: String?,
        refFile: String?
    ): VNSpace {
        var space =
            localSpaceDao.getSpaceByRemoteId(
                authCache.loggedInUser!!.localUser.userId,
                remoteSpaceId
            )
        if (space == null) {
            space = LocalSpace(
                0,
                remoteSpaceId,
                authCache.loggedInUser!!.localUser.userId,
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
            isPrivate,
            System.currentTimeMillis()
        )
    }

    private suspend fun persistNetworkSpace(networkSpace: NetworkSpace): VNSpace =
        persistNetworkSpace(networkSpace.spaceID, networkSpace.private, null, null)

    override suspend fun quitAllSpaces(): Boolean {
        val userId = authCache.loggedInUser?.localUser?.userId
        val remoteUserId = authCache.loggedInUser?.localUser?.userId
        if (userId == null || remoteUserId == null) return false

        val spaces = localSpaceDao.getAllSpacesWithUser(userId)
        for (spaceId in spaces) {
            spaceService.quitSpace(remoteUserId)
        }
        deleteAllSpaces()
        return true
    }

    // needed for delete user as explicit quitting is not necessary for delete user
    override suspend fun deleteAllSpaces(): Boolean {
        val userId = authCache.loggedInUser?.localUser?.userId ?: return false
        withContext(Dispatchers.IO) {
            localFileDao.deleteAllFilesOfUser(userId)
            localSpaceDao.quitAllSpaces(userId)
        }
        return true
    }
}