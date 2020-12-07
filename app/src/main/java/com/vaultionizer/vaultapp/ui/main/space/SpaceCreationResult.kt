package com.vaultionizer.vaultapp.ui.main.space

import com.vaultionizer.vaultapp.data.model.domain.VNSpace

data class SpaceCreationResult(
    val createdSpace: VNSpace? = null,
    val creationSuccessful: Boolean = false
)
