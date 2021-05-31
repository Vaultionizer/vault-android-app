package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import com.vaultionizer.vaultapp.data.model.rest.user.LoggedInUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Logs in a user on the remote server and stores the returned tokens in the injected singleton
     * instance of [com.vaultionizer.vaultapp.data.cache.AuthCache].
     *
     * @see LoggedInUser
     * @see LocalUser
     *
     * @param host      The hostname of the remote server.
     * @param username  The username.
     * @param password  The raw password.
     * @return          An instance of [LoggedInUser] that represents the logged in user.
     */
    suspend fun login(
        host: String,
        username: String,
        password: String
    ): Flow<Resource<LoggedInUser>>

    /**
     * Creates a new user on the remote server and creates an entry for that user in the local
     * database.
     *
     * @see LoggedInUser
     * @see LocalUser
     *
     * @param host      The hostname of the remote server.
     * @param username  The username.
     * @param password  The raw password.
     * @param authKey   The auth-key provided by the system administrator of [host].
     * @return          An instance of [LoggedInUser] that represents the newly created and already
     *                  logged in user.
     */
    suspend fun register(
        host: String,
        username: String,
        password: String,
        authKey: String
    ): Flow<Resource<LoggedInUser>>

    /**
     * Logs out the current user.
     *
     * @return True if the logout was successful, false otherwise.
     */
    suspend fun logout(): Boolean

    /**
     * Deletes the current user from the remote server and all information related to the user from
     * the local database.
     *
     * @return True if the deletion was successful, false otherwise.
     */
    suspend fun deleteUser(): Boolean
}