package com.vaultionizer.vaultapp.ui.main.file

import android.app.Activity
import androidx.fragment.app.Fragment
import com.vaultionizer.vaultapp.R
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import dev.shreyaspatil.MaterialDialog.interfaces.DialogInterface

enum class FileAlertDialogType(
    val titleTextId: Int,
    val messageTextId: Int?,
    val positiveButtonTextId: Int = R.string.all_confirm,
    val negativeButtonTextId: Int? = R.string.all_cancel,
    val positiveIcon: Int = R.drawable.ic_baseline_check_white_24,
    val negativeIcon: Int? = R.drawable.ic_baseline_clear_white_24
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
    );

    fun createDialog(
        activity: Activity,
        positiveClick: (DialogInterface, Int) -> Unit,
        negativeClick: ((DialogInterface, Int) -> Unit)? = null
    ): MaterialDialog {
        val dialog = MaterialDialog.Builder(activity)
            .setTitle(activity.getString(titleTextId))
            .setPositiveButton(
                activity.getString(positiveButtonTextId),
                positiveIcon
            ) { inf, which ->
                positiveClick(inf, which)
            }

        negativeButtonTextId?.let {
            if (negativeIcon != null) {
                dialog.setNegativeButton(
                    activity.getString(negativeButtonTextId),
                    negativeIcon
                ) { inf, which ->
                    negativeClick?.let {
                        negativeClick(inf, which)
                    }
                }
            } else {
                dialog.setNegativeButton(
                    activity.getString(negativeButtonTextId),
                ) { inf, which ->
                    negativeClick?.let {
                        negativeClick(inf, which)
                    }
                }
            }
        }

        messageTextId?.let {
            dialog.setMessage(activity.getString(messageTextId))
        }

        return dialog.build()
    }
}

fun Fragment.createDialog(
    type: FileAlertDialogType, positiveClick: (DialogInterface, Int) -> Unit
): MaterialDialog = type.createDialog(requireActivity(), positiveClick)

fun Fragment.createDialog(
    type: FileAlertDialogType, positiveClick: (DialogInterface, Int) -> Unit,
    negativeClick: (DialogInterface, Int) -> Unit
): MaterialDialog = type.createDialog(requireActivity(), positiveClick, negativeClick)

fun Fragment.showDialog(
    type: FileAlertDialogType, positiveClick: (DialogInterface, Int) -> Unit
) {
    createDialog(type, positiveClick).show()
}

fun Fragment.showDialog(
    type: FileAlertDialogType, positiveClick: (DialogInterface, Int) -> Unit,
    negativeClick: (DialogInterface, Int) -> Unit
) {
    createDialog(type, positiveClick, negativeClick).show()
}