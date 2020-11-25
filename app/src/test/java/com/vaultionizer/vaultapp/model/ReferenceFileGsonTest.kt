package com.vaultionizer.vaultapp.model

import com.google.gson.GsonBuilder
import com.vaultionizer.vaultapp.data.model.rest.rf.*
import com.vaultionizer.vaultapp.util.external.RuntimeTypeAdapterFactory
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ReferenceFileGsonTest {

    val EXAMPLE_REF_FILE =
        """
                {
                    "version": 1,
                    "files": [
                        {
                            "type": "directory",
                            "name": "Documents"
                        }
                    ]
                }
            """.trimIndent()

    @Test
    fun testReferenceFileDeserializationFromGson() {
        val factory = RuntimeTypeAdapterFactory.of(Element::class.java, "type", true)
            .registerSubtype(File::class.java, "file")
            .registerSubtype(Folder::class.java, "directory")

        val gson = GsonBuilder()
            .registerTypeAdapterFactory(factory)
            .create()

        var refFile = gson.fromJson<ReferenceFile>(EXAMPLE_REF_FILE, ReferenceFile::class.java)
        assertEquals(refFile?.version, 1)
        assertEquals(refFile?.elements?.size, 1)
        assertEquals(refFile?.elements?.get(0)?.name, "Documents")
        assertEquals(refFile?.elements?.get(0)?.type, Type.FOLDER)
    }
}