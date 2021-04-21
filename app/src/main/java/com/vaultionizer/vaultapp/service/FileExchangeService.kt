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
                    AuthRepository.user?.localUser?.endpoint
                )
            )

            // Send STOMP data
            uploadSession.send(
                StompSendHeaders(
                    "/api/ws/upload", customHeaders = mapOf(
                        Pair(
                            "userID",
                            AuthRepository.user!!.localUser.remoteUserId.toString()
                        ),
                        Pair("spaceID", spaceRemoteId.toString()),
                        Pair("saveIndex", fileRemoteId.toString()),
                        Pair("sessionKey", AuthRepository.user!!.sessionToken)
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
                    AuthRepository.user?.localUser?.endpoint
                )
            )

            val channel = String.format(
                DOWNLOAD_CHANNEL,
                AuthRepository.user?.webSocketToken
            )

            val headers = StompSubscribeHeaders(
                channel, customHeaders = mapOf(
                    "userID" to AuthRepository.user?.localUser?.remoteUserId.toString(),
                    "sessionKey" to AuthRepository.user?.sessionToken.toString()
                )
            )

            fileService.downloadFile(DownloadFileRequest(fileRemoteId, spaceRemoteId))
            val downloadedFile = downloadSession.subscribe(headers).first()

            downloadSession.disconnect()
            return@withContext downloadedFile.body?.bytes
        }
    }
}