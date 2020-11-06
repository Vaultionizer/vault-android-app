package com.vaultionizer.vaultapp.hilt

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.BufferedSink
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
object RestModule {

    var sessionToken: String = ""
    var host: String = "v2202006123966120989.bestsrv.de"
        set(value) {
            field = value.toHttpUrl().host
            relativePath = value.toHttpUrl().pathSegments.joinToString("/")
        }
    var relativePath: String = "api/"

    @Provides
    @ActivityRetainedScoped
    fun provideOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request()
            if(request.body == null || request.body?.contentType()?.subtype?.contains("json") == false) {
                return@addInterceptor it.proceed(request.newBuilder().url(injectHostUrl(request)).build())
            }

            val requestJsonObj = JSONObject(requestBodyToString(request.body))
            requestJsonObj.put("sessionKey", sessionToken)

            val body = requestJsonObj.toString().toRequestBody(request.body!!.contentType())

            println("Host: $host")
            return@addInterceptor it.proceed(request.newBuilder().url(injectHostUrl(request)).post(body).build())
        }.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }).build()

    @Provides
    @ActivityRetainedScoped
    fun provideRetrofitBase(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://$host/$relativePath")
            .addConverterFactory(GsonConverterFactory.create())
            .client(provideOkHttpClient())
            .build()
    }

    private fun requestBodyToString(body: RequestBody?): String {
        if(body == null) {
            return ""
        }

        val buffer = Buffer()
        body.writeTo(buffer)

        return buffer.readUtf8()
    }

    private fun injectHostUrl(request: Request): HttpUrl =
        request.url.newBuilder().host(host).scheme("https").addPathSegments(relativePath).build()

}