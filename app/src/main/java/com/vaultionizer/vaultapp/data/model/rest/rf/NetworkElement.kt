package com.vaultionizer.vaultapp.data.model.rest.rf

import com.vaultionizer.vaultapp.data.model.domain.VNFile

abstract class NetworkElement {
    abstract val type: Type
    abstract val name: String
    abstract val id: Long
}