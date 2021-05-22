package com.vaultionizer.vaultapp.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

fun writeFileToInternal(context: Context, path: String, data: ByteArray) {
    context.openFileOutput(path, Context.MODE_PRIVATE).use {
        it.write(data)
        it.close()
    }
}

fun readFileFromInternal(context: Context, path: String): ByteArray =
    context.openFileInput(path).readBytes()

fun deleteFileFromInternal(context: Context, path: String) = context.deleteFile(path)

fun buildVaultionizerFilePath(fileId: Long) = "$fileId.${Constants.VN_FILE_SUFFIX}"

/**
 * Modified version of:
 * https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content
 */
fun ContentResolver.getFileName(uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor? = query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}