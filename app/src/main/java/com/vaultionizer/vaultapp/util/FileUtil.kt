package com.vaultionizer.vaultapp.util

import android.content.Context

fun writeFileToInternal(context: Context, path: String, data: ByteArray) {
    context.openFileOutput(path, Context.MODE_PRIVATE).use {
        it.write(data)
        it.close()
    }
}