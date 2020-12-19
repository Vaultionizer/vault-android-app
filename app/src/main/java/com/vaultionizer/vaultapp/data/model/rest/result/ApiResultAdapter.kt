package com.vaultionizer.vaultapp.data.model.rest.result

import android.util.Log
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class ApiResultAdapter(val type: Type) : CallAdapter<Type, Call<ApiResult<Type>>> {

    override fun responseType(): Type {
        return type
    }

    override fun adapt(call: Call<Type>): Call<ApiResult<Type>> {
        Log.e("Vault", "ADAPT")
        return ApiResultCallDecorator(call)
    }
}