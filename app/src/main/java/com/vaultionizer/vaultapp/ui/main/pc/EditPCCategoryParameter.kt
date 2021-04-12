package com.vaultionizer.vaultapp.ui.main.pc

import java.io.Serializable

data class EditPCCategoryParameter constructor(val categoryName: String, val categoryId: Int = -1) :
    Serializable {

}