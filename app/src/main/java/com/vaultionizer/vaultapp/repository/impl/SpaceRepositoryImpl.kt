package com.vaultionizer.vaultapp.repository.impl

import android.util.Log
import com.google.gson.Gson
import com.vaultionizer.vaultapp.cryptography.CryptoUtils
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import com.vaultionizer.vaultapp.data.cache.AuthCache
import com.vaultionizer.vaultapp.data.db.dao.LocalFileDao
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import com.vaultionizer.vaultapp.data.db.dao.SharedSpaceSecretDao
import com.vaultionizer.vaultapp.data.db.entity.LocalSpace
import com.vaultionizer.vaultapp.data.db.entity.SharedSpaceSecret
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.data.model.rest.request.CreateSpaceRequest
import com.vaultionizer.vaultapp.data.model.rest.request.JoinSpaceRequest
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
    val sharedSecretDao : SharedSpaceSecretDao,
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
        authKeyAccess: Boolean,
        algorithm: String,
        password: String?
    ): Flow<Resource<VNSpace>> {
        if (CryptoUtils.existsKey(peekNextSpaceId())) {
            CryptoUtils.deleteKey(peekNextSpaceId())
        }

        val cryptoMode = if (algorithm.contains("GCM")) CryptoMode.GCM else CryptoMode.CBC
        val nextSpaceId = peekNextSpaceId()
        if (isPrivate) {
            CryptoUtils.generateKeyForSingleUserSpace(
                nextSpaceId,
                CryptoType.AES,
                cryptoMode,
                CryptoPadding.NoPadding
            )
        } else {
            val secret = CryptoUtils.generateKeyForSharedSpace(
                nextSpaceId,
                CryptoType.AES,
                cryptoMode,
                CryptoPadding.NoPadding,
                password!!
            )
            withContext(Dispatchers.IO) {
                sharedSecretDao.createSharedSecret(SharedSpaceSecret(nextSpaceId, secret))
            }
            Log.e("Vault", "Test "+secret.size)
        }

        return flow {
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
                        true,
                        name
                    )

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
        isOwner: Boolean,
        name: String?
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
            isOwner,
            System.currentTimeMillis()
        )
    }

    private suspend fun persistNetworkSpace(networkSpace: NetworkSpace): VNSpace =
        persistNetworkSpace(networkSpace.spaceID, networkSpace.private, true, null)

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

    override suspend fun getSpaceSecret(spaceId: Long): Flow<Resource<SharedSpaceSecret>> {
        return flow {
            val res = sharedSecretDao.getSecret(spaceId)
            if (res != null){
                emit(Resource.Success(res))
            }
            else {
                emit(Resource.Error(-1))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun joinSpace(remoteSpaceId: Long, spaceId: Long, authKey: String, name: String): Flow<Resource<Boolean>> {
        return flow {
            val joinRes = spaceService.join(JoinSpaceRequest(authKey), remoteSpaceId)
            when(joinRes){
                is ApiResult.Success -> {
                    localSpaceDao.createSpace(LocalSpace(spaceId, remoteSpaceId, authCache.loggedInUser?.localUser?.userId!!, name, null, 0))
                    emit(Resource.Success(true))
                }
                else -> {
                    // TODO error handling
                    emit(Resource.Error(-1))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

}
