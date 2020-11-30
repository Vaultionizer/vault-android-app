package com.vaultionizer.vaultapp.data.model.rest.refFile

import com.google.gson.annotations.SerializedName
import com.thedeanda.lorem.LoremIpsum
import kotlin.random.Random
import kotlin.random.nextInt

data class NetworkReferenceFile(
        val version: Int,
        @SerializedName("files") val elements: MutableList<NetworkElement>
) {
        companion object {
                private const val CURRENT_VERSION = 1
                var GLOBAL_FOLDER_ID_COUNTER: Long = -1

                val EMPTY_FILE = NetworkReferenceFile(CURRENT_VERSION, mutableListOf())

                fun generateRandom(): NetworkReferenceFile {
                        val depth = Random.nextInt(1..5)

                        val root = NetworkFolder(Type.FOLDER, "", GLOBAL_FOLDER_ID_COUNTER--, null, null)
                        fillFolder(root, depth)

                        return NetworkReferenceFile(1, root.content!!)
                }

                fun fillFolder(folder: NetworkFolder, depth: Int) {
                        if(depth == 0) {
                                return
                        }

                        val loremIpsum = LoremIpsum.getInstance()
                        val amount = Random.nextInt(3, 10)
                        val content = mutableListOf<NetworkElement>()

                        for(i in 0 until amount) {
                                val type = (Math.random() * 10).toInt()
                                val element: NetworkElement = when(type) {
                                        in 0..2 -> {
                                                val child = NetworkFolder(
                                                        name = loremIpsum.getWords(1, 3),
                                                        createdAt = randomDate(),
                                                        content = mutableListOf(),
                                                        id = GLOBAL_FOLDER_ID_COUNTER--
                                                )
                                                fillFolder(child, depth - 1)
                                                child
                                        }
                                        else -> {
                                                NetworkFile(
                                                        name = loremIpsum.getWords(1),
                                                        size = Random.nextInt(
                                                                1,
                                                                1000000
                                                        ).toLong(),
                                                        crc = "Nice CRC",
                                                        id = Random.nextInt(
                                                                0,
                                                                Integer.MAX_VALUE
                                                        ).toLong(),
                                                        updatedAt = randomDate(),
                                                        createdAt = randomDate()
                                                )
                                        }
                                }

                                content.add(element)
                        }

                        folder.content = content
                }

                fun randomDate(): Long {
                        return System.currentTimeMillis() - (Math.random() * 1000 * 60 * 60 * 24 * 30).toLong()
                }
        }
}