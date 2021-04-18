package com.vaultionizer.vaultapp.repository

import android.content.Context
import android.util.Log
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

    fun deletePair(pairId: Int){
        for(pairIdx in pairs.indices){
            if (pairs[pairIdx].id == pairId){
                pairs.removeAt(pairIdx)
                return
            }
        }
    }

    fun deleteOnlyCategory(categoryId: Int){
        for (pair in pairs){
            if (pair.categoryId != categoryId) continue
            pair.categoryId = null
        }
        for (categoryIdx in categories.indices){
            if (categories[categoryIdx].id == categoryId){
                categories.removeAt(categoryIdx)
                return
            }
        }
    }

    fun deleteCategoryAndPairs(categoryId: Int){
        for (pairIdx in pairs.size..0){
            if (pairs[pairIdx].categoryId != categoryId) continue
            pairs.removeAt(pairIdx)
        }
        for (categoryIdx in categories.indices){
            if (categories[categoryIdx].id == categoryId){
                categories.removeAt(categoryIdx)
                return
            }
        }
    }

    fun getCategoryIdByPos(pos: Int): Int? {
        if (categories.size > pos && pos >= 0) {
            return categories[pos].id
        }
        return null
    }

    fun getCategoryPosById(categoryId: Int?): Int{
        if (categoryId == null) return 0
        for (cat in categories.indices){
            if (categories[cat].id == categoryId) return cat + 1
        }
        return 0
    }

    fun getCatgoryNames(): Array<String>{
        return Array(categories.size + 1) {
            if (it == 0) "<Uncategorized>"
            else categories[it - 1].name
        }
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
            categoryIdsUsed.add(categories.last().id)
        }
        return true
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

    private fun findUnusedCategoryId(): Int {
        for (i in 0..(categories.size + 1)) {
            if (!categoryIdsUsed.contains(i)){
                return i
            }
        }
        return -1
    }

}