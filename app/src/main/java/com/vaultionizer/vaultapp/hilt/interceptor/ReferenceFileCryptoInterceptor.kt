package com.vaultionizer.vaultapp.hilt.interceptor

import android.util.Base64
import com.vaultionizer.vaultapp.cryptography.CryptoUtils
import com.vaultionizer.vaultapp.repository.SpaceRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONObject

class ReferenceFileCryptoInterceptor(private val spaceRepository: SpaceRepository) : Interceptor {

    companion object {
        const val REF_FILE_SUFFIX = "api/refFile/{remoteSpaceId}/read"
        const val CREATE_USER_SUFFIX = "api/user/create"
        val CONTENT_MEDIA_TYPE = "application/json; charset=utf-8".toMediaTypeOrNull()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val urlString = request.url.toString()
        var spaceId: Long

        val bodyKey = when {
            urlString.endsWith(REF_FILE_SUFFIX) -> {
                spaceId = getUrlPartFromBack(urlString, 2).toLong()
                "content"
            }
            // Can only occur during user creation
            urlString.endsWith(CREATE_USER_SUFFIX) -> {
                runBlocking {
                    spaceId = spaceRepository.peekNextSpaceId()
                }
                "refFile"
            }
            else -> {
                return chain.proceed(request)
            }
        }

        val modifiedRequest = handleRequest(request, bodyKey, spaceId)
        if (bodyKey == "refFile") {
            return handleResponse(chain.proceed(modifiedRequest), spaceId)
        }
        return chain.proceed(modifiedRequest)
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
            bodyKey, Base64.encode(
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
        val decryptedRefFile = CryptoUtils.decryptData(spaceId, encryptedRefFile)

        return response.newBuilder().body(decryptedRefFile.toResponseBody(CONTENT_MEDIA_TYPE))
            .build()
    }

    private fun getUrlPartFromBack(string: String, index: Int): String {
        val splitted = string.split("/")
        return splitted[splitted.size - index]
    }
}