package com.vaultionizer.vaultapp.service

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.model.rest.request.UploadFileRequest
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ManagedResult
import com.vaultionizer.vaultapp.repository.AuthRepository
import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompCommand
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage
import javax.inject.Inject

class FileExchangeService @Inject constructor(
    val authRepository: AuthRepository,
    val fileService: FileService,
    val gson: Gson
) {

    companion object {
        const val WEB_SOCKET_URL_TEMPLATE = "https://%s:443/gs-guide-websocket"
    }

    private lateinit var stompClient: StompClient
    private var disposable = CompositeDisposable()

    suspend fun uploadFile(spaceRemoteId: Long, data: ByteArray): Flow<ApiResult<Long>> {
        return flow {
            checkConnection()
            val response = fileService.uploadFile(UploadFileRequest(1, spaceRemoteId))

            Log.e("Vault", response.javaClass.name)
            when(response) {
                is ApiResult.Success -> {
                    val fileId = response.data
                    Log.e("Vault", "Responded with sace Index ${fileId}")

                    val dis = stompClient.send(
                        StompMessage(
                            StompCommand.SEND,
                            listOf(
                                StompHeader(StompHeader.DESTINATION, "/api/ws/upload"),
                                StompHeader(
                                    "userID",
                                    AuthRepository.user!!.localUser.remoteUserId.toString()
                                ),
                                StompHeader("spaceID", spaceRemoteId.toString()),
                                StompHeader("saveIndex", fileId.toString()),
                                StompHeader("sessionKey", AuthRepository.user!!.sessionToken)
                            ),
                            JSONObject().apply {
                                put("content", data.toString())
                            }.toString()
                        )
                    ).compose(applySchedulers()).subscribe({
                        Log.e("Vault", "SUCCESS")
                    }, {
                        Log.e("Vault", it.localizedMessage)
                    })

                    disposable.add(dis)
                    emit(ApiResult.Success(fileId))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun checkConnection() {
        if(!this::stompClient.isInitialized || !stompClient.isConnected) {
            Log.e("Vault", "Connecting...")
            disposable.dispose()
            disposable = CompositeDisposable()

            stompClient = Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                String.format(WEB_SOCKET_URL_TEMPLATE, AuthRepository.user?.localUser?.endpoint)
            )

            Log.e("Vault", "URL ${String.format(WEB_SOCKET_URL_TEMPLATE, AuthRepository.user?.localUser?.endpoint)}")

            stompClient.withClientHeartbeat(1000).withServerHeartbeat(1000)

            val dispLifecycle: Disposable = stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when(it.type) {
                        LifecycleEvent.Type.OPENED -> {
                            Log.e("Vault", "OPEN")
                        }
                        LifecycleEvent.Type.ERROR -> {
                            Log.e("Vault", "Stomp connection error", it.getException());
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            Log.e("Vault", "Closed", it.exception)
                        }
                        LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                            Log.e("Vault", "Heartbeat")
                        }
                    }
                }

            disposable.add(dispLifecycle)

            stompClient.connect()
            Log.e("Vault", "Connected ${stompClient.isConnected}")
        }

        Log.e("Vault", "End of checkConnection")
    }

    private fun applySchedulers(): CompletableTransformer? {
        return CompletableTransformer { upstream: Completable ->
            upstream
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

}