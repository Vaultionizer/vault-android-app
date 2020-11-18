package com.vaultionizer.vaultapp.hilt

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vaultionizer.vaultapp.data.model.rest.result.ApiCallFactory
import com.vaultionizer.vaultapp.data.model.rest.rf.Element
import com.vaultionizer.vaultapp.data.model.rest.rf.File
import com.vaultionizer.vaultapp.data.model.rest.rf.Folder
import com.vaultionizer.vaultapp.repository.AuthRepository
import com.vaultionizer.vaultapp.util.external.RuntimeTypeAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
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

@Module
@InstallIn(ActivityRetainedComponent::class)
object RestModule {

    var host: String = "v2202006123966120989.bestsrv.de"
        set(value) {
            field = value.toHttpUrl().host
            relativePath = value.toHttpUrl().pathSegments.joinToString("/")
        }
    var relativePath: String = "api/"

    @Provides
    @ActivityRetainedScoped
    fun provideRetrofitBase(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://$host/$relativePath")
            .addConverterFactory(GsonConverterFactory.create(provideGson()))
            .addCallAdapterFactory(ApiCallFactory())
            .client(provideOkHttpClient())
            .build()
    }

    @Provides
    @ActivityRetainedScoped
    fun provideGson(): Gson {
        val factory = RuntimeTypeAdapterFactory.of(Element::class.java, "type")
            .registerSubtype(File::class.java, "file")
            .registerSubtype(Folder::class.java, "directory")

        return GsonBuilder()
            .registerTypeAdapterFactory(factory)
            .create()
    }

    @Provides
    @ActivityRetainedScoped
    fun provideOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request()
            if(request.body == null || request.body?.contentType()?.subtype?.contains("json") == false) {
                return@addInterceptor it.proceed(request.newBuilder().url(injectHostUrl(request)).build())
            }

            var jsonBody = JSONObject(requestBodyToString(it.request().body))
            jsonBody.put("auth", JSONObject().apply {
                put("sessionKey", AuthRepository.user?.sessionToken)
            })

            val requestBody = jsonBody.toString().toRequestBody(request.body!!.contentType())
            return@addInterceptor it.proceed(request.newBuilder().url(injectHostUrl(request)).post(requestBody).build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }).build()


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