package com.vaultionizer.vaultapp.data.model.rest.result

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
        // Return type should be Call<*>
        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        // Query generic type A of Call<A>
        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != ApiResult::class.java) {
            return null
        }

        // Query generic type B of Call<ApiResult<B>>
        val callGenericType = getParameterUpperBound(0, callType as ParameterizedType)
        return ApiResultAdapter(callGenericType, getRawType(callGenericType))
    }
}