package com.vaultionizer.vaultapp.hilt

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vaultionizer.vaultapp.data.model.rest.result.ApiCallFactory
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFile
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFolder
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.util.external.RuntimeTypeAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RestModule {

    var host: String = "v2202006123966120989.bestsrv.de"
        set(value) {
            field = value.toHttpUrl().host
            relativePath = value.toHttpUrl().pathSegments.joinToString("/")
        }
    var relativePath: String = ""

    @Provides
    @Singleton
    fun provideRetrofitBase(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://$host")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(ApiCallFactory())
            .client(provideOkHttpClient())
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val factory = RuntimeTypeAdapterFactory.of(NetworkElement::class.java, "type", true)
            .registerSubtype(NetworkFile::class.java, "file")
            .registerSubtype(NetworkFolder::class.java, "directory")

        return GsonBuilder()
            .registerTypeAdapterFactory(factory)
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request()
            if(request.body == null || request.body?.contentType()?.subtype?.contains("json") == false) {
                Log.v("Vault", "Different content type...")
                return@addInterceptor it.proceed(request.newBuilder().url(injectHostUrl(request)).build())
            }

            Log.v("Vault", "Injecting auth object...")

            var jsonBody = JSONObject(requestBodyToString(it.request().body))
            jsonBody.put("auth", JSONObject().apply {
                put("sessionKey", AuthRepository.user?.sessionToken)
                put("userID", AuthRepository.user?.localUser?.remoteUserId)
                Log.e("Vault", "User: ${AuthRepository.user?.localUser?.remoteUserId} Token: ${AuthRepository.user?.sessionToken.toString()}")
            })

            val requestBody = jsonBody.toString().toRequestBody(request.body!!.contentType())

            Log.v("Vault", "Proceed chain... $host $relativePath ${injectHostUrl(request).toUri().toString()}")
            return@addInterceptor it.proceed(request.newBuilder().url(injectHostUrl(request)).post(requestBody).build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }).connectTimeout(20000, TimeUnit.MILLISECONDS).build()

    private fun requestBodyToString(body: RequestBody?): String {
        if(body == null || body.contentLength() == 0L) {
            return "{}"
        }

        val buffer = Buffer()
        body.writeTo(buffer)

        if(buffer.size == 0L) {
            return "{}"
        }

        return buffer.readUtf8()
    }

    private fun injectHostUrl(request: Request): HttpUrl =
        request.url.newBuilder().host(host).scheme("https").addPathSegments(relativePath).build()

}