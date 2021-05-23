package com.vaultionizer.vaultapp.ui.main.file

import android.net.Uri
import com.vaultionizer.vaultapp.data.model.domain.VNFile
import com.vaultionizer.vaultapp.data.model.domain.VNSpace

sealed class FileEvent {

    data class EncryptionKeyRequired(
        val space: VNSpace
    ) : FileEvent()

    open class UploadFileNameConflict(
        val file: VNFile,
        val fsSource: Uri
    ) : FileEvent()

}
