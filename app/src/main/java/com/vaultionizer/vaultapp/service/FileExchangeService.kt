package com.vaultionizer.vaultapp.service

import android.util.Base64
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.model.rest.request.DownloadFileRequest
import com.vaultionizer.vaultapp.repository.AuthRepository
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

class FileExchangeService @Inject constructor(
    val authRepository: AuthRepository,
    val fileService: FileService,
    val gson: Gson
) {

    companion object {
        const val WEB_SOCKET_TEMPLATE = "${Constants.DEFAULT_PROTOCOL}://%s/wss/websocket"
        const val DOWNLOAD_CHANNEL = "/api/wsres/download/%s"
    }

    private var stompClient = StompClient(OkHttpWebSocketClient())

    suspend fun uploadFile(spaceRemoteId: Long, fileRemoteId: Long, data: ByteArray) {
        withContext(Dispatchers.IO) {
            // Connect to server
            val uploadSession = stompClient.connect(
                String.format(
                    WEB_SOCKET_TEMPLATE,
                    authRepository.loggedInUser?.localUser?.endpoint
                )
            )

            // Send STOMP data
            uploadSession.send(
                StompSendHeaders(
                    "/api/ws/upload", customHeaders = mapOf(
                        Pair(
                            "userID",
                            authRepository.loggedInUser!!.localUser.remoteUserId.toString()
                        ),
                        Pair("spaceID", spaceRemoteId.toString()),
                        Pair("saveIndex", fileRemoteId.toString()),
                        Pair("sessionKey", authRepository.loggedInUser!!.sessionToken),
                        Pair("websocketToken", authRepository.loggedInUser!!.webSocketToken)
                    )
                ), FrameBody.Text(JSONObject().apply {
                    put("content", Base64.encodeToString(data, Base64.NO_WRAP))
                }.toString())
            )

            // Close session
            uploadSession.disconnect()
        }
    }

    suspend fun downloadFile(spaceRemoteId: Long, fileRemoteId: Long): ByteArray? {
        return withContext(Dispatchers.IO) {
            val downloadSession = stompClient.connect(
                String.format(
                    WEB_SOCKET_TEMPLATE,
                    authRepository.loggedInUser!!.localUser?.endpoint
                )
            )

            val channel = String.format(
                DOWNLOAD_CHANNEL,
                authRepository.loggedInUser!!.webSocketToken
            )

            val headers = StompSubscribeHeaders(
                channel, customHeaders = mapOf(
                    "userID" to authRepository.loggedInUser?.localUser?.remoteUserId.toString(),
                    "sessionKey" to authRepository.loggedInUser?.sessionToken.toString()
                )
            )

            val downloadedFileFlow = downloadSession.subscribe(headers)
            fileService.downloadFile(DownloadFileRequest(fileRemoteId, spaceRemoteId))
            val downloadedFile = downloadedFileFlow.first()

            downloadSession.disconnect()
            return@withContext downloadedFile.body?.bytes
        }
    }
}