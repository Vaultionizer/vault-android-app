package com.vaultionizer.vaultapp.data.db.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.vaultionizer.vaultapp.data.db.entity.LocalFile
import com.vaultionizer.vaultapp.data.db.entity.LocalSpace

data class SpaceWithFiles(
    @Embedded val space: LocalSpace,

    @Relation(
        parentColumn = "space_id",
        entityColumn = "space_id"
    )
    val files: List<LocalFile>
)
