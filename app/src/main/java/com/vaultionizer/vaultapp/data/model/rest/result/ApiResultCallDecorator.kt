package com.vaultionizer.vaultapp.data.model.rest.result

import android.util.Log
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiResultCallDecorator<T : Any>(
    val proxy: Call<T>,
    val typeClass: Class<*>
) : Call<ApiResult<T>> {

    override fun clone(): Call<ApiResult<T>> =
        ApiResultCallDecorator(proxy.clone(), typeClass)

    override fun execute(): Response<ApiResult<T>> =
        throw UnsupportedOperationException("Synchronous API calls are not allowed.");

    override fun enqueue(callback: Callback<ApiResult<T>>) {
        Log.e("Vault", "ENQUEUE")
        proxy.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.code() in (200 until 300)) {
                    if (typeClass.equals(Unit::class.java) || typeClass.equals(Nothing::class.java)) {
                        callback.onResponse(
                            this@ApiResultCallDecorator,
                            Response.success(ApiResult.Success(Unit) as ApiResult<T>)
                        )
                    } else {
                        callback.onResponse(
                            this@ApiResultCallDecorator,
                            Response.success(ApiResult.Success(response.body()!!))
                        )
                    }
                } else {
                    callback.onResponse(
                        this@ApiResultCallDecorator,
                        Response.success(ApiResult.Error(statusCode = response.code()))
                    )
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onResponse(
                    this@ApiResultCallDecorator,
                    Response.success(ApiResult.NetworkError(t))
                )
            }
        })
    }

    override fun isExecuted() = proxy.isExecuted

    override fun cancel() = proxy.cancel()

    override fun isCanceled(): Boolean = proxy.isCanceled

    override fun request(): Request = proxy.request()

    override fun timeout(): Timeout = proxy.timeout()

}