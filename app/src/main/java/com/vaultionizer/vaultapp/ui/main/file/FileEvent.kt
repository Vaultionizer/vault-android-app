package com.vaultionizer.vaultapp.ui.main.file

import com.vaultionizer.vaultapp.data.model.domain.VNSpace

sealed class FileEvent {

    data class EncryptionKeyRequired(
        val space: VNSpace
    ) : FileEvent()

}
