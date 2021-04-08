package com.vaultionizer.vaultapp.ui.main.file

import com.vaultionizer.vaultapp.R

enum class FileAlertDialogType(
    val titleTextId: Int,
    val contentText: Int,
    val confirmText: Int
) {

    DELETE_FILE(
        R.string.delete_file_progress_title,
        R.string.delete_file_progress_content,
        R.string.delete_file_progress_confirm
    )
}