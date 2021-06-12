package com.vaultionizer.vaultapp.model.data

import com.vaultionizer.vaultapp.data.cache.FileCache
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import org.junit.Assert.assertEquals
import org.junit.Test

class FileCacheTest {

    private val EXAMPLE_FILE = VNFile(
        name = "Hello World",
        space = VNSpace(
            0,
            0,
            0,
            "Nice Space",
            0,
            true,
            System.currentTimeMillis()
        ),
        null,
        5000,
        9,
        null
    )

    @Test
    fun testFileCache_emptyCache() {
        val cache = FileCache()

        assertEquals(null, cache.rootFile)
        assertEquals(null, cache.spaceId)
        assertEquals(null, cache.getFile(0))
    }

    @Test
    fun testFileCache_addFiles_localStrategy() {
        val cache = FileCache(FileCache.IdCachingStrategy.LOCAL_ID)
        cache.addFile(EXAMPLE_FILE)

        assertEquals(null, cache.rootFile)
        assertEquals(null, cache.spaceId)
        assertEquals(EXAMPLE_FILE, cache.getFile(5000))
        assertEquals(
            EXAMPLE_FILE,
            cache.getFileByStrategy(5000, FileCache.IdCachingStrategy.LOCAL_ID)
        )
    }

    @Test
    fun testFileCache_addFiles_remoteStrategy() {
        val cache = FileCache(FileCache.IdCachingStrategy.REMOTE_ID)
        cache.addFile(EXAMPLE_FILE)

        assertEquals(null, cache.rootFile)
        assertEquals(0L, cache.spaceId)
        assertEquals(EXAMPLE_FILE, cache.getFile(9))
        assertEquals(
            EXAMPLE_FILE,
            cache.getFileByStrategy(9, FileCache.IdCachingStrategy.REMOTE_ID)
        )
    }

    @Test
    fun testFileCache_deleteFile_remoteStrategy() {
        val cache = FileCache(FileCache.IdCachingStrategy.REMOTE_ID)
        cache.addFile(EXAMPLE_FILE)
        cache.deleteFile(EXAMPLE_FILE)

        assertEquals(cache.getFile(EXAMPLE_FILE.remoteId!!), null)
    }

    @Test
    fun testFileCache_deleteFile_localStrategy() {
        val cache = FileCache(FileCache.IdCachingStrategy.LOCAL_ID)
        cache.addFile(EXAMPLE_FILE)
        cache.deleteFile(EXAMPLE_FILE)

        assertEquals(cache.getFile(EXAMPLE_FILE.localId!!), null)
    }

}