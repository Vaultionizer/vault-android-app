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
    ),

    DELETE_PAIR(
        R.string.delete_file_progress_title,
        R.string.delete_pair_content,
        R.string.delete_pair_confirm
    ),

    DELETE_ONLY_CATEGORY(
        R.string.delete_file_progress_title,
        R.string.delete_only_category_content,
        R.string.delete_only_category_confirm
    ),

    DELETE_CATEGORY_AND_PAIRS(
        R.string.delete_file_progress_title,
        R.string.delete_category_and_pairs_content,
        R.string.delete_category_and_pairs_confirm
    ),

    SAVE_FILE(
        R.string.save_pc_file_title,
        R.string.save_pc_file_content,
        R.string.save_pc_file_confirmation
    )
}

