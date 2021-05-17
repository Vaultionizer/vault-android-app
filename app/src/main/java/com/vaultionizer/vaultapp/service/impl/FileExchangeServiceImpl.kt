package com.vaultionizer.vaultapp.service.impl

import android.util.Base64
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.cache.AuthCache
import com.vaultionizer.vaultapp.service.FileExchangeService
import com.vaultionizer.vaultapp.service.FileService
import com.vaultionizer.vaultapp.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.headers.*
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject
import javax.inject.Inject

class FileExchangeServiceImpl @Inject constructor(
    val authCache: AuthCache,
    val fileService: FileService,
    val gson: Gson
) : FileExchangeService {

    companion object {
        const val WEB_SOCKET_TEMPLATE = "${Constants.DEFAULT_PROTOCOL}://%s/wss/websocket"
        const val DOWNLOAD_CHANNEL = "/api/wsres/download/%s"
    }

    private var stompClient = StompClient(OkHttpWebSocketClient())

    override suspend fun uploadFile(spaceRemoteId: Long, fileRemoteId: Long, data: ByteArray) {
        withContext(Dispatchers.IO) {
            // Connect to server
            val uploadSession = stompClient.connect(
                String.format(
                    WEB_SOCKET_TEMPLATE,
                    authCache.loggedInUser?.localUser?.endpoint
                )
            )

            // Send STOMP data
            uploadSession.send(
                StompSendHeaders(
                    "/api/ws/upload", customHeaders = mapOf(
                        Pair(
                            "userID",
                            authCache.loggedInUser!!.localUser.remoteUserId.toString()
                        ),
                        Pair("spaceID", spaceRemoteId.toString()),
                        Pair("saveIndex", fileRemoteId.toString()),
                        Pair("sessionKey", authCache.loggedInUser!!.sessionToken),
                        Pair("websocketToken", authCache.loggedInUser!!.webSocketToken)
                    )
                ), FrameBody.Text(JSONObject().apply {
                    put("content", Base64.encodeToString(data, Base64.NO_WRAP))
                }.toString())
            )

            // Close session
            uploadSession.disconnect()
        }
    }

    override suspend fun downloadFile(spaceRemoteId: Long, fileRemoteId: Long): ByteArray? {
        return withContext(Dispatchers.IO) {
            val downloadSession = stompClient.connect(
                String.format(
                    WEB_SOCKET_TEMPLATE,
                    authCache.loggedInUser!!.localUser.endpoint
                )
            )

            val channel = String.format(
                DOWNLOAD_CHANNEL,
                authCache.loggedInUser!!.webSocketToken
            )

            val headers = StompSubscribeHeaders(
                channel, customHeaders = mapOf(
                    "userID" to authCache.loggedInUser?.localUser?.remoteUserId.toString(),
                    "sessionKey" to authCache.loggedInUser?.sessionToken.toString()
                )
            )

            val downloadedFileFlow = downloadSession.subscribe(headers)
            fileService.downloadFile(spaceRemoteId, fileRemoteId)
            val downloadedFile = downloadedFileFlow.first()

            downloadSession.disconnect()
            return@withContext Base64.decode(downloadedFile.body?.bytes, Base64.NO_WRAP)
        }
    }
}