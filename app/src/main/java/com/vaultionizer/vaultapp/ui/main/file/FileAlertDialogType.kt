package com.vaultionizer.vaultapp.ui.main.file

import android.content.Context
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.vaultionizer.vaultapp.R

enum class FileAlertDialogType(
    val titleTextId: Int,
    val messageTextId: Int?,
    val positiveButtonTextId: Int = R.string.all_confirm,
    val negativeButtonTextId: Int? = R.string.all_cancel,
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
        R.string.delete_category_and_pairs_confirm,
    ),

    SAVE_FILE(
        R.string.save_pc_file_title,
        R.string.save_pc_file_content,
        R.string.save_pc_file_confirmation
    );

    fun createDialog(
        activity: Context,
        positiveClick: (MaterialDialog) -> Unit,
        negativeClick: ((MaterialDialog) -> Unit)? = null
    ): MaterialDialog {
        val dialog = MaterialDialog(activity)
            .title(titleTextId)
            .positiveButton(positiveButtonTextId, click = { dialog ->
                positiveClick(dialog)
                dialog.dismiss()
            })

        if (negativeButtonTextId != null) {
            dialog.negativeButton(negativeButtonTextId, click = { dialog ->
                if (negativeClick != null) {
                    negativeClick(dialog)
                }
                dialog.dismiss()
            })
        }

        messageTextId?.let {
            dialog.message(messageTextId)
        }

        return dialog
    }
}

fun Fragment.createDialog(
    type: FileAlertDialogType, positiveClick: (MaterialDialog) -> Unit
): MaterialDialog = type.createDialog(requireContext(), positiveClick)

fun Fragment.createDialog(
    type: FileAlertDialogType, positiveClick: (MaterialDialog) -> Unit,
    negativeClick: (MaterialDialog) -> Unit
): MaterialDialog = type.createDialog(requireContext(), positiveClick, negativeClick)

fun Fragment.showDialog(
    type: FileAlertDialogType, positiveClick: (MaterialDialog) -> Unit
) {
    createDialog(type, positiveClick).show()
}

fun Fragment.showDialog(
    type: FileAlertDialogType, positiveClick: (MaterialDialog) -> Unit,
    negativeClick: (MaterialDialog) -> Unit
) {
    createDialog(type, positiveClick, negativeClick).show()
}