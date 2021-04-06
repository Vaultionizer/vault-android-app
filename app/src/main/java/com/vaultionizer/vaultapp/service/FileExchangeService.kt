package com.vaultionizer.vaultapp.service

import android.util.Base64
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.model.rest.request.UploadFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
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
    private lateinit var uploadSession: StompSession

    suspend fun uploadFile(spaceRemoteId: Long, data: ByteArray): Flow<ApiResult<Long>> {
        return flow {
            val response = fileService.uploadFile(UploadFileRequest(1, spaceRemoteId))
            when (response) {
                is ApiResult.Success -> {
                    val fileId = response.data

                    // Connect to server
                    uploadSession = stompClient.connect(
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
                                Pair("saveIndex", fileId.toString()),
                                Pair("sessionKey", AuthRepository.user!!.sessionToken)
                            )
                        ), FrameBody.Text(JSONObject().apply {
                            put("content", Base64.encodeToString(data, Base64.NO_WRAP))
                        }.toString())
                    )

                    // Emit new file
                    emit(ApiResult.Success(fileId))
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}