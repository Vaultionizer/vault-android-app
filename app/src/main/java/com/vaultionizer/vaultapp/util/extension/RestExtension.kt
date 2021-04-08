package com.vaultionizer.vaultapp.util.extension

import com.vaultionizer.vaultapp.data.model.rest.result.ApiResult
import com.vaultionizer.vaultapp.data.model.rest.result.ApiResultCallDecorator

inline fun <reified T : Any> ApiResultCallDecorator<T>.constructSuccess(body: T?): ApiResult.Success<T> {
    return when (T::class) {
        Unit::class -> ApiResult.Success(Unit) as ApiResult.Success<T>
        else -> ApiResult.Success(body!!)
    }
}