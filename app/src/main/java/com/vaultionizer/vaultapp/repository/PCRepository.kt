package com.vaultionizer.vaultapp.repository

import android.content.Context
import com.google.gson.Gson
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace
import com.vaultionizer.vaultapp.data.pc.PCCategory
import com.vaultionizer.vaultapp.data.pc.PCFile
import com.vaultionizer.vaultapp.data.pc.PCPair
import java.util.HashSet
import javax.inject.Inject

class PCRepository @Inject constructor(
    val gson: Gson,
    val fileRepository: FileRepository
) {
    private var fileName: String = ""
    private var categories: ArrayList<PCCategory> = ArrayList()
    private var pairs: ArrayList<PCPair> = ArrayList()
    private var categoryIdsUsed: HashSet<Int> = HashSet()

    fun createNewFile(name: String){
        fileName = name
        categories.clear()
        pairs.clear()
        categoryIdsUsed.clear()
    }

    fun getCurrentFile(): PCFile{
        return PCFile(categories, pairs)
    }

    suspend fun saveFile(space: VNSpace, parent: VNFile?, context: Context){
        fileRepository.uploadFile(space, parent, gson.toJson(PCFile(categories, pairs)).toByteArray(), fileName, context)
    }

    fun replacePair(newKey: String, newValue: String, newCategoryId: Int?, id: Int): Boolean{
        for (pairIdx in pairs.indices){
            if (pairs[pairIdx].id == id){
                pairs[pairIdx] = PCPair(id, newKey, newValue, newCategoryId)
                return true
            }
        }
        return false
    }

    fun getCatgoryNames(): Array<String>{
        return Array(categories.size + 1) {
            if (it == 0) "<Uncategorized>"
            else categories[it - 1].name
        }
    }

    fun getPairById(id: Int): PCPair?{
        for (pair in pairs){
            if (pair.id == id) return pair
        }
        return null
    }

    fun addNewPair(key: String, value: String, categoryId: Int?){
        val newID = if (pairs.isEmpty()) 0 else pairs.last().id+1
        pairs.add(PCPair(newID, key, value, categoryId))
    }

    fun addCategory(name: String, categoryId: Int? = null): Boolean{
        if (categoryId == null && findCategoryByName(name) != null) return false
        if (categoryId != null) {
            val index = findCategoryById(categoryId) ?: return false
            categories[index] = PCCategory(categoryId, name)
        }
        else{
            categories.add(PCCategory(findUnusedCategoryId(), name))
        }
        return true
    }

    fun getCategory(id: Int): PCCategory? {
        for( category in categories){
            if (category.id == id) return category
        }
        return null
    }

    fun findCategoryByName(name: String): Int? {
        for (i in categories.indices){
            if (categories[i].name == name) return i
        }
        return null
    }

    private fun findCategoryById(id: Int): Int? {
        for (i in categories.indices){
            if (categories[i].id == id) return i
        }
        return null
    }

    private fun findUnusedCategoryId(): Int{
        for (i in categories.size..0) {
            if (!categoryIdsUsed.contains(i)){
                return i
            }
        }
        return 0
    }

}