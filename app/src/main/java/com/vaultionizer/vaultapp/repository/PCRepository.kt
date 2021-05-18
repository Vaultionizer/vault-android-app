package com.vaultionizer.vaultapp.repository

import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.pc.PCFile

interface PCRepository {
    val changed: Boolean

    fun createNewFile(name: String)

    fun reset()

    fun getCurrentFile(): PCFile

    suspend fun saveFile(parent: VNFile)

    fun replacePair(newKey: String, newValue: String, newCategoryId: Int?, id: Int): Boolean

    fun deletePair(pairId: Int)

    fun deleteOnlyCategory(categoryId: Int)

    fun deleteCategoryAndPairs(categoryId: Int)

    fun getCategoryIdByPos(pos: Int): Int?

    fun getCategoryPosById(categoryId: Int?): Int

    fun getCatgoryNames(): Array<String>

    fun addNewPair(key: String, value: String, categoryId: Int?)

    fun addCategory(name: String, categoryId: Int? = null): Boolean

    fun findCategoryByName(name: String): Int?
}