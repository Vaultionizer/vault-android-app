package com.vaultionizer.vaultapp.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.vaultionizer.vaultapp.data.cache.DecryptionResultCache
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.OutputStream
import kotlin.concurrent.thread

class VaultionizerContentProvider : ContentProvider() {

    companion object {
        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI("com.vaultionizer.vaultapp.provider", "file/#", 1)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface VaultionizerContentProviderEntryPoint {
        fun decryptionResultCache(): DecryptionResultCache
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val appContext = context?.applicationContext ?: throw IllegalStateException()
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(
                appContext,
                VaultionizerContentProviderEntryPoint::class.java
            )

        val fileId = uri.lastPathSegment!!.toLong()
        val decryptionCache = hiltEntryPoint.decryptionResultCache()
        val decryptionResult = decryptionCache.getResultByFileId(fileId)
                ?: return null
        val pipe = ParcelFileDescriptor.createPipe()

        thread(start = true) {
            decryptionCache.annotateResultAsShown(fileId)
            sendDecryptedData(
                    decryptionResult,
                    ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])
            )
        }

        return pipe[0]
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        TODO("Not yet implemented")
    }

    private fun sendDecryptedData(data: ByteArray, stream: OutputStream) {
        stream.write(data)
        stream.flush()
        stream.close()
    }
}