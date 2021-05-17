package com.vaultionizer.vaultapp.hilt

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vaultionizer.vaultapp.data.cache.AuthCache
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFile
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFolder
import com.vaultionizer.vaultapp.data.model.rest.result.ApiCallFactory
import com.vaultionizer.vaultapp.util.Constants
import com.vaultionizer.vaultapp.util.external.RuntimeTypeAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RestModule {

    var host: String = "v2202006123966120989.bestsrv.de"
        set(value) {
            field = value.toHttpUrl().host
            relativePath = value.toHttpUrl().pathSegments.joinToString("/")
            port = value.toHttpUrl().port
        }
    var relativePath: String = ""
    var port: Int = 443

    @Provides
    @Singleton
    fun provideRetrofitBase(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://$host:$port/$relativePath")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(ApiCallFactory())
            .client(okHttpClient)
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
    fun provideOkHttpClient(authCache: AuthCache) = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request()
            val xAuthHeader = JSONObject()
                .put("sessionKey", authCache.loggedInUser?.sessionToken)
                .put("userID", authCache.loggedInUser?.localUser?.remoteUserId)

            return@addInterceptor it.proceed(
                request.newBuilder()
                    .url(injectHostUrl(request))
                    .header("xAuth", xAuthHeader.toString()).build()
            )
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).connectTimeout(20000, TimeUnit.MILLISECONDS).build()

    private fun requestBodyToString(body: RequestBody?): String {
        if (body == null || body.contentLength() == 0L) {
            return "{}"
        }

        val buffer = Buffer()
        body.writeTo(buffer)

        if (buffer.size == 0L) {
            return "{}"
        }

        return buffer.readUtf8()
    }

    private fun injectHostUrl(request: Request): HttpUrl =
        request.url.newBuilder().host(host).port(port).scheme(Constants.DEFAULT_PROTOCOL).addPathSegments(relativePath).build()

}