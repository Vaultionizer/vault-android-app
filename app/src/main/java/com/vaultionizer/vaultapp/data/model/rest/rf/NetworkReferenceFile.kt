package com.vaultionizer.vaultapp.data.model.rest.rf

import com.google.gson.annotations.SerializedName
import com.thedeanda.lorem.LoremIpsum
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

data class NetworkReferenceFile(
        val version: Int,
        @SerializedName("files") val elements: MutableList<NetworkElement>
) {
        companion object {
                const val CURRENT_VERSION = 1
                val EMPTY_FILE = NetworkReferenceFile(CURRENT_VERSION, mutableListOf())

                fun generateRandom(): NetworkReferenceFile {
                        val depth = Random.nextInt(2..5)

                        val root = NetworkFolder(Type.FOLDER, "", null, null)
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
                                        in 0..9 -> {
                                                val child = NetworkFolder(
                                                        name = loremIpsum.getWords(1, 3),
                                                        createdAt = randomDate(),
                                                        content = mutableListOf()
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
                                                        crc = "",
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

                fun randomDate(): String {
                        val dfDateTime =
                                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        val year: Int = Random.nextInt(
                                1900,
                                2013
                        )
                        val month: Int = Random.nextInt(0, 11)

                        val gc = GregorianCalendar(year, month, 1)
                        val day: Int =
                                 Random.nextInt(1, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH))

                        gc.set(year, month, day)

                        return dfDateTime.format(gc.getTime())
                }
        }
}