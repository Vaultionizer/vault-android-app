package com.vaultionizer.vaultapp.hilt.interceptor

import android.util.Base64
import com.vaultionizer.vaultapp.cryptography.CryptoUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.json.JSONObject

class ReferenceFileCryptoInterceptor : Interceptor {

    companion object {
        const val REF_FILE_SUFFIX = "api/refFile/{remoteSpaceId}/read"
        const val CREATE_USER_SUFFIX = "api/user/create"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val urlString = request.url.toString()

        val bodyKey = when {
            urlString.endsWith(REF_FILE_SUFFIX) -> {
                "content"
            }
            urlString.endsWith(CREATE_USER_SUFFIX) -> {
                "refFile"
            }
            else -> {
                return chain.proceed(request)
            }
        }

        val modifiedRequest = handleRequest(request, bodyKey)
        return handleResponse(chain.proceed(modifiedRequest))
    }

    private fun handleRequest(request: Request, bodyKey: String): Request {
        val buffer = Buffer()
        request.body!!.writeTo(buffer)
        val jsonBody = JSONObject(buffer.readUtf8())

        val encryptedRefFile = CryptoUtils.encryptData()
        jsonBody.put(bodyKey, Base64.encode())
    }

    private fun handleResponse(response: Response): Response {
        response.request.
    }
}