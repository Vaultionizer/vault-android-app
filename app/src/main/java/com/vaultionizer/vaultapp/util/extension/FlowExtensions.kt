package com.vaultionizer.vaultapp.util.extension

import com.vaultionizer.vaultapp.data.model.rest.result.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull

/**
 * TODO(jatsqi): Remove this ugly hack
 */
suspend fun <T : Any> Flow<Resource<T>>.collectSuccess(): T? {
    return (filter {
        it is Resource.Success
    }.firstOrNull() as Resource.Success?)?.data
}