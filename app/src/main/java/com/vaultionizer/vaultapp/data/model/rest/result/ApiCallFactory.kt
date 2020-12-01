package com.vaultionizer.vaultapp.data.model.rest.result

import android.util.Log
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApiCallFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        Log.e("Vault", "FACTORY")
        if(getRawType(returnType) != Call::class.java) {
            Log.e("Vault", "NOT CALL")
            return null
        }

        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if(getRawType(callType) != ApiResult::class.java) {
            Log.e("Vault", "NOT APIRESULT ${callType.javaClass.canonicalName}")
            return null
        }

        val callGenericType = getParameterUpperBound(0, callType as ParameterizedType)
        return ApiResultAdapter(callGenericType)
    }
}