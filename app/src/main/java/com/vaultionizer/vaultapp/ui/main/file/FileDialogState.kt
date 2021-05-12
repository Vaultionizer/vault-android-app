package com.vaultionizer.vaultapp.ui.main.file

data class FileDialogState(
    val fileError: Int? = null,
    val fileAlertType: FileAlertDialogType? = null,
    val isValid: Boolean = false
)
