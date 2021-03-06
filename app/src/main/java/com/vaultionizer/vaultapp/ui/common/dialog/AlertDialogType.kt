package com.vaultionizer.vaultapp.ui.common.dialog

import android.content.Context
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.vaultionizer.vaultapp.R

enum class AlertDialogType(
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
    ),

    DELETE_SPACE(
        R.string.delete_space_title,
        R.string.delete_space_body,
        R.string.delete_space_button_text,
    ),

    KICK_ALL_USERS(
        R.string.kick_users_title,
        R.string.kick_users_body,
        R.string.kick_users_button_text,
    ),

    QUIT_SPACE(
        R.string.quit_space_title,
        R.string.quit_space_body,
        R.string.quit_space_button_text,
    ),

    MAKE_SPACE_PRIVATE(
        R.string.space_private_title,
        R.string.space_private_body,
        R.string.space_private_button_text,
    ),

    REGENERATE_AUTH_KEY(
        R.string.regen_auth_key_title,
        R.string.regen_auth_key_body,
        R.string.regen_auth_key_button_text,
    ),

    DELETE_LOCAL_FILES(
        R.string.delete_local_files_title,
        R.string.delete_local_files_body,
        R.string.delete_local_files_button_text,
    ),

    DELETE_USER(
        R.string.delete_user_title,
        R.string.delete_user_body,
        R.string.delete_user_button_text,
    ),

    QUIT_ALL_SPACES(
        R.string.quit_all_spaces_title,
        R.string.quit_all_spaces_body,
        R.string.quit_all_spaces_button_text,
    ),

    REQUEST_KEY_GENERATION(
        R.string.file_viewer_request_key_title,
        R.string.file_viewer_request_key_message,
        R.string.file_viewer_request_key_positive,
        R.string.file_viewer_request_key_negative
    ),

    SHOW_AUTH_KEY(
        R.string.really_show_auth_key_title,
        R.string.really_show_auth_key_body,
        R.string.really_show_auth_key_positive,
        R.string.really_show_auth_key_negative
    ),

    SAVE_AUTH_KEY(
        R.string.really_save_auth_key_title,
        R.string.really_save_auth_key_body,
        R.string.really_save_auth_key_positive,
        R.string.really_save_auth_key_negative
    ),

    COPY_AUTH_KEY(
        R.string.really_copy_auth_key_title,
        R.string.really_copy_auth_key_body,
        R.string.really_copy_auth_key_positive,
        R.string.really_copy_auth_key_negative
    ),

    UPLOAD_OR_REPLACE(
        R.string.file_viewer_replace_or_upload_dialog_title,
        R.string.file_viewer_replace_or_upload_dialog_message,
        R.string.file_viewer_replace_or_upload_dialog_positive,
        R.string.file_viewer_replace_or_upload_dialog_negative
    ),

    EXCHANGE_ERROR(
        R.string.file_viewer_exchange_error_title,
        R.string.file_viewer_exchange_error_message,
        R.string.file_viewer_exchange_error_positive,
        null
    ),

    NO_APP_AVAILABLE_TO_OPEN_FILE(
        R.string.file_viewer_open_file_no_app_available_title,
        R.string.file_viewer_open_file_no_app_available_message,
        R.string.file_viewer_open_file_no_app_available_positive,
        null
    );

    fun createDialog(
        activity: Context,
        positiveClick: (MaterialDialog) -> Unit,
        negativeClick: ((MaterialDialog) -> Unit)? = null
    ): MaterialDialog {
        val resultDialog = MaterialDialog(activity)
            .title(titleTextId)
            .positiveButton(positiveButtonTextId, click = { dialog ->
                positiveClick(dialog)
                dialog.dismiss()
            })

        if (negativeButtonTextId != null) {
            resultDialog.negativeButton(negativeButtonTextId, click = { dialog ->
                if (negativeClick != null) {
                    negativeClick(dialog)
                }
                dialog.dismiss()
            })
        }

        messageTextId?.let {
            resultDialog.message(messageTextId)
        }

        return resultDialog
    }
}

fun Fragment.createDialog(
    type: AlertDialogType, positiveClick: (MaterialDialog) -> Unit
): MaterialDialog = type.createDialog(requireContext(), positiveClick)

fun Fragment.createDialog(
    type: AlertDialogType, positiveClick: (MaterialDialog) -> Unit,
    negativeClick: (MaterialDialog) -> Unit
): MaterialDialog = type.createDialog(requireContext(), positiveClick, negativeClick)

fun Fragment.showDialog(
    type: AlertDialogType, positiveClick: (MaterialDialog) -> Unit
) {
    createDialog(type, positiveClick).show()
}

fun Fragment.showDialog(
    type: AlertDialogType, positiveClick: (MaterialDialog) -> Unit,
    negativeClick: (MaterialDialog) -> Unit
) {
    createDialog(type, positiveClick, negativeClick).show()
}