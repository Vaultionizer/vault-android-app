package com.vaultionizer.vaultapp.data.db.entity.converters

import androidx.room.TypeConverter
import com.vaultionizer.vaultapp.data.db.entity.LocalFile
import com.vaultionizer.vaultapp.data.db.entity.LocalFileSyncRequest

class EnumConverters {

    @TypeConverter
    fun fromSyncRequestType(type: LocalFileSyncRequest.Type) = type.id

    @TypeConverter
    fun toSyncRequestType(id: Int) =
        LocalFileSyncRequest.Type.values().first { it.id == id }

    @TypeConverter
    fun fromFileType(type: LocalFile.Type) = type.id

    @TypeConverter
    fun toFileType(id: Int) =
        LocalFile.Type.values().first { it.id == id }

}