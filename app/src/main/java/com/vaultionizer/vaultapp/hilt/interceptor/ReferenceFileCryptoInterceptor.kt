package com.vaultionizer.vaultapp.hilt.interceptor

import android.util.Base64
import com.vaultionizer.vaultapp.cryptography.CryptoUtils
import com.vaultionizer.vaultapp.data.cache.AuthCache
import com.vaultionizer.vaultapp.data.db.dao.LocalSpaceDao
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONObject

class ReferenceFileCryptoInterceptor(
    private val spaceDao: LocalSpaceDao,
    private val authCache: AuthCache
) : Interceptor {

    companion object {
        val READ_REF_FILE_PATTERN = ".*/refFile/\\d+/read/".toRegex()
        val UPDATE_REF_FILE_PATTERN = ".*/refFile/\\d+/update/".toRegex()
        val CREATE_USER_PATTERN = ".*/api/user/create/".toRegex()
        val CONTENT_MEDIA_TYPE = "application/json; charset=utf-8".toMediaTypeOrNull()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val urlString = request.url.toString()

        when {
            urlString.matches(READ_REF_FILE_PATTERN) -> {
                val remoteSpaceId = getUrlPartFromBack(urlString, 3).toLong()
                val spaceId = querySpaceId(remoteSpaceId) ?: return chain.proceed(request)

                return handleResponse(chain.proceed(request), spaceId)
            }
            urlString.matches(UPDATE_REF_FILE_PATTERN) -> {
                val remoteSpaceId = getUrlPartFromBack(urlString, 3).toLong()
                val spaceId = querySpaceId(remoteSpaceId) ?: return chain.proceed(request)

                return chain.proceed(handleRequest(request, "content", spaceId))
            }
            // Can only occur during user creation
            urlString.matches(CREATE_USER_PATTERN) -> {
                var spaceId: Long
                runBlocking {
                    spaceId = spaceDao.getNextSpaceId()
                }

                return chain.proceed(handleRequest(request, "refFile", spaceId))
            }
            else -> {
                return chain.proceed(request)
            }
        }
    }

    private fun handleRequest(request: Request, bodyKey: String, spaceId: Long): Request {
        val buffer = Buffer()
        request.body!!.writeTo(buffer)
        val jsonBody = JSONObject(buffer.readUtf8())

        val encryptedRefFile = CryptoUtils.encryptData(
            spaceId,
            jsonBody.getString(bodyKey).toByteArray()
        )
        jsonBody.put(
            bodyKey, Base64.encodeToString(
                encryptedRefFile, Base64.NO_WRAP
            )
        )

        return request.newBuilder().method(
            request.method,
            jsonBody.toString().toRequestBody(CONTENT_MEDIA_TYPE)
        ).build()
    }

    private fun handleResponse(response: Response, spaceId: Long): Response {
        if (response.code != 200) {
            return response
        }

        if ((response.body?.contentLength() ?: 0) == 0L) {
            return response
        }

        val encryptedRefFile = response.body?.bytes() ?: ByteArray(0)
        val decryptedRefFile =
            CryptoUtils.decryptData(spaceId, Base64.decode(encryptedRefFile, Base64.NO_WRAP))


        var decryptedRefFileValidJson =
            String(decryptedRefFile, Charsets.UTF_8)
        // Ugly hack to bypass missing crypto functionalities.
        decryptedRefFileValidJson = decryptedRefFileValidJson.replace("\u0000", "")

        return response.newBuilder()
            .body(decryptedRefFileValidJson.toResponseBody(CONTENT_MEDIA_TYPE))
            .build()
    }

    @Suppress("SameParameterValue")
    private fun getUrlPartFromBack(string: String, index: Int): String {
        val splitted = string.split("/")
        return splitted[splitted.size - index]
    }

    private fun querySpaceId(remoteSpaceId: Long): Long? {
        return runBlocking {
            val user = authCache.loggedInUser ?: return@runBlocking null
            val space = spaceDao.getSpaceByRemoteId(user.localUser.userId, remoteSpaceId)
                ?: return@runBlocking null

            return@runBlocking space.spaceId
        }
    }
}