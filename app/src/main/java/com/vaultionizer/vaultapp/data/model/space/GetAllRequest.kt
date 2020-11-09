package com.vaultionizer.vaultapp.data.model.space

data class GetAllRequest(
    val spaceID : Long,
    val creator : Boolean,
    val isPrivate : Boolean
)