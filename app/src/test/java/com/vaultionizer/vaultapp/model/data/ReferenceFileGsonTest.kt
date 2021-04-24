package com.vaultionizer.vaultapp.model.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkElement
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFile
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkFolder
import com.vaultionizer.vaultapp.data.model.rest.refFile.NetworkReferenceFile
import com.vaultionizer.vaultapp.util.external.RuntimeTypeAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ReferenceFileGsonTest {

    private val EXAMPLE_REF_FILE =
        """
                {
                    "version": 1,
                    "files": [
                        {
                            "type": "directory",
                            "name": "Documents",
                            "id": -2,
                            "createdAt": 1234,
                            "content": []
                        },
                        {
                            "type": "directory",
                            "name": "Photos",
                            "id": -3,
                            "createdAt": 1234,
                            "content": [
                                {
                                    "type": "file",
                                    "name": "Vacation.png",
                                    "id": 20,
                                    "crc": "nicecrc",
                                    "size": 5,
                                    "createdAt": 4321,
                                    "updatedAt": 4322
                                }
                            ]
                        }
                    ]
                }
            """.trimIndent().replace(" ", "").replace("\n", "")

    private lateinit var gson: Gson

    @Before
    fun initGson() {
        val factory = RuntimeTypeAdapterFactory.of(NetworkElement::class.java, "type", true)
            .registerSubtype(NetworkFile::class.java, "file")
            .registerSubtype(NetworkFolder::class.java, "directory")

        gson = GsonBuilder()
            .registerTypeAdapterFactory(factory)
            .create()
    }

    @Test
    fun testReferenceFileGson_Serialize() {
        val refFile = NetworkReferenceFile(
            1, mutableListOf(
                NetworkFolder(
                    id = -2,
                    name = "Documents",
                    createdAt = 1234,
                    content = mutableListOf()
                ),
                NetworkFolder(
                    id = -3,
                    name = "Photos",
                    createdAt = 1234,
                    content = mutableListOf(
                        NetworkFile(
                            name = "Vacation.png",
                            id = 20,
                            crc = "nicecrc",
                            size = 5,
                            createdAt = 4321,
                            updatedAt = 4322
                        )
                    )
                )
            )
        )

        assertEquals(gson.toJson(refFile), EXAMPLE_REF_FILE)
    }

    @Test
    fun testReferenceFileGson_Deserialize() {
        var refFile = gson.fromJson(EXAMPLE_REF_FILE, NetworkReferenceFile::class.java)

        assertEquals(refFile?.version, 1)
        assertEquals(refFile?.elements?.size, 2)

        val documents = refFile?.elements?.get(0) as NetworkFolder
        assertEquals(documents.name, "Documents")
        assertEquals(documents.id, -2)
        assertEquals(documents.createdAt, 1234)

        val photos = refFile.elements[1] as NetworkFolder
        assertEquals(photos.name, "Photos")
        assertEquals(photos.id, -3)
        assertEquals(photos.createdAt, 1234)
        assertEquals(photos.content?.size, 1)

        val vacation = photos.content?.get(0) as NetworkFile
        assertEquals(vacation.name, "Vacation.png")
        assertEquals(vacation.id, 20)
        assertEquals(vacation.crc, "nicecrc")
        assertEquals(vacation.size, 5)
        assertEquals(vacation.createdAt, 4321)
        assertEquals(vacation.updatedAt, 4322)
    }
}