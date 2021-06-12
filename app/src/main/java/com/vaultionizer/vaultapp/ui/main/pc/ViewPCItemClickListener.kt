package com.vaultionizer.vaultapp.ui.main.pc

import com.vaultionizer.vaultapp.data.model.pc.PCCategory
import com.vaultionizer.vaultapp.data.model.pc.PCPair

interface ViewPCItemClickListener {
    fun openPairOptions(pair: PCPair)
    fun openCategoryOptions(category: PCCategory)
}