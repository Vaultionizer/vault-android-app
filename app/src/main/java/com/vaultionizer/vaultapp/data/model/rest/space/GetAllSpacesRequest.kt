package com.vaultionizer.vaultapp.data.model.rest.space

data class GetAllSpacesRequest(
    val spaceID: Long,
    val creator: Boolean,
    val isPrivate: Boolean
)