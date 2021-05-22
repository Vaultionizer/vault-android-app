package com.vaultionizer.vaultapp.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.vaultionizer.vaultapp.data.model.domain.VNFile

/**
 * Writes a file to the internal app storage.
 *
 * @param path      Path of the file.
 * @param data      Data.
 */
private fun Context.writeFile(path: String, data: ByteArray) {
    openFileOutput(path, Context.MODE_PRIVATE).use {
        it.write(data)
        it.close()
    }
}

/**
 * Writes a file to the internal app storage by using its local file Id.
 *
 * @param fileId    Local Id of the file.
 * @param data      The content of the file.
 */
fun Context.writeFile(fileId: Long, data: ByteArray) {
    writeFile(buildVaultionizerFilePath(fileId), data)
}

/**
 * Reads a file from the internal app storage.
 *
 * @param path      Path of the file.
 * @return          The content of the file.
 */
private fun Context.readFile(path: String): ByteArray =
    openFileInput(path).readBytes()

/**
 * Reads a file from the internal app storage by using its local file Id.
 *
 * @param fileId    Local Id of the file.
 * @return          The content of the file.
 */
fun Context.readFile(fileId: Long): ByteArray = readFile(buildVaultionizerFilePath(fileId))

/**
 * Deletes a file from the internal app storage by using its local file Id.
 *
 * @param fileId    Local Id of the file.
 */
fun Context.deleteFile(fileId: Long) = deleteFile(buildVaultionizerFilePath(fileId))

/**
 * Queries the absolute file path of a specific local file.
 *
 * @param fileId    Id of the local file.
 */
fun Context.getAbsoluteFilePath(fileId: Long) =
    getFileStreamPath(buildVaultionizerFilePath(fileId)).toUri()

private fun buildVaultionizerFilePath(fileId: Long) = "$fileId.${Constants.VN_FILE_SUFFIX}"

/**
 * Checks if a folder already contains a file with [name] and adds an increasing index until the
 * the name is unique.
 *
 * @param parent    Corresponding folder.
 * @param name      Name to check.
 * @return          [name] if the name is unique, otherwise an index is added in front of the name.
 */
fun resolveFileNameConflicts(parent: VNFile, name: String): String {
    val nameSet = parent.content?.map { it.name }?.toSet() ?: return name

    nameSet.forEach {
        if (it == name) {
            var currentIndex = 1
            while (nameSet.contains(buildDuplicateFileName(name, currentIndex)))
                ++currentIndex

            return buildDuplicateFileName(name, currentIndex)
        }
    }

    return name
}

/**
 * Appends a increasing index to a file name to eliminate duplicated names.
 *
 * @param name      Original name of the file.
 * @param index     Increasing index.
 */
private fun buildDuplicateFileName(name: String, index: Int) = "($index) $name"

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