package com.vaultionizer.vaultapp.data.model.rest.rf

import com.google.gson.annotations.SerializedName
import com.thedeanda.lorem.LoremIpsum
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

data class ReferenceFile(
        val version: Int,
        @SerializedName("files") val elements: MutableList<Element>
) {
        companion object {
                const val CURRENT_VERSION = 1
                val EMPTY_FILE = ReferenceFile(CURRENT_VERSION, mutableListOf())

                fun generateRandom(): ReferenceFile {
                        val depth = Random.nextInt(2..5)

                        val root = Folder(Type.FOLDER, "", null, null)
                        fillFolder(root, depth)

                        return ReferenceFile(1, root.content!!)
                }

                fun fillFolder(folder: Folder, depth: Int) {
                        if(depth == 0) {
                                return
                        }

                        val loremIpsum = LoremIpsum.getInstance()
                        val amount = Random.nextInt(3, 10)
                        val content = mutableListOf<Element>()

                        for(i in 0 until amount) {
                                val type = (Math.random() * 10).toInt()
                                val element: Element = when(type) {
                                        in 0..3 -> {
                                                val child = Folder(
                                                        name = loremIpsum.getWords(1, 3),
                                                        createdAt = randomDate(),
                                                        content = mutableListOf()
                                                )
                                                fillFolder(child, depth - 1)
                                                child
                                        }
                                        else -> {
                                                File(
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