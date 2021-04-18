package com.vaultionizer.vaultapp.service

import android.util.Base64
import com.google.gson.Gson
import com.vaultionizer.vaultapp.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject
import javax.inject.Inject

class FileExchangeService @Inject constructor(
    val authRepository: AuthRepository,
    val fileService: FileService,
    val gson: Gson
) {

    companion object {
        const val WEB_SOCKET_URL_TEMPLATE = "https://%s:443/wss/websocket"
    }

    private var stompClient = StompClient(OkHttpWebSocketClient())

    suspend fun uploadFile(spaceRemoteId: Long, fileRemoteId: Long, data: ByteArray) {
        withContext(Dispatchers.IO) {
            // Connect to server
            val uploadSession = stompClient.connect(
                String.format(
                    WEB_SOCKET_URL_TEMPLATE,
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

    suspend fun downloadFile(spaceRemoteId: Long, fileRemoteId: Long) {
        withContext(Dispatchers.IO) {
            val downloadSession = stompClient.connect(
                String.format(
                    WEB_SOCKET_URL_TEMPLATE,
                    AuthRepository.user?.localUser?.endpoint
                )
            )

            // downloadSession.subscribe()
        }
    }
}