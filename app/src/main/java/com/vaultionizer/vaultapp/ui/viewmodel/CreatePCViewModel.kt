package com.vaultionizer.vaultapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.repository.PCRepository
import com.vaultionizer.vaultapp.ui.main.pc.InputFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreatePCViewModel @Inject constructor(
    val pcRepository: PCRepository
) : ViewModel() {
    private val _pcCreationRes = MutableLiveData<InputFormState>()
    val pcCreationRes: LiveData<InputFormState> = _pcCreationRes


    fun pcNameChanged(text: String) {
        val name = text.trim()

        if (name.length < 4) {
            _pcCreationRes.value = InputFormState(R.string.create_pc_too_short_name, false)
        } else {
            _pcCreationRes.value = InputFormState(null, true)
        }
    }

    fun createPersonalContainer(name: String) {
        pcRepository.createNewFile(name)
    }

    // for testing purposes only
    fun addTestData() {
        val testCategories = arrayOf(
            "Financial data",
            "Phone numbers",
            "To do items",
            "Clothing sizes",
            "Stuff to watch"
        )
        val categoryIds: ArrayList<Int> = ArrayList()
        for (cat in testCategories) {
            pcRepository.addCategory(cat)
            val id = pcRepository.getCategoryIdByPos(pcRepository.getCatgoryNames().size - 2)
            if (id == null) continue
            categoryIds.add(id)
        }

        // financial data
        pcRepository.addNewPair("Trading ref", "abcde", categoryIds[0])
        pcRepository.addNewPair("IBAN giro", "DE31 4159 2653 5897 32", categoryIds[0])
        pcRepository.addNewPair("IBAN trading", "DE38 4626 4338 3279 50", categoryIds[0])

        // phone numbers
        pcRepository.addNewPair("Ricardo", "0187 132434535", categoryIds[1])
        pcRepository.addNewPair("Mario", "0187 415454485", categoryIds[1])

        // To do items
        pcRepository.addNewPair("Finish SE", "Sunday evening", categoryIds[2])
        pcRepository.addNewPair("Japanese shukudai", "Tuesday", categoryIds[2])

        // Clothing stuff
        pcRepository.addNewPair("Shoe size", "46", categoryIds[3])
        pcRepository.addNewPair("pants size", "45", categoryIds[3])

        // Stuff to watch
        pcRepository.addNewPair("Aot", "Waiting for it", categoryIds[4])
        pcRepository.addNewPair("Bnha", "Every saturday", categoryIds[4])
    }
}