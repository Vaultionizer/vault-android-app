package com.vaultionizer.vaultapp.util.qr

import android.util.Log
import java.lang.Exception
import java.util.zip.CRC32

class CRC32Handler {

    fun parsePayload(content: String): String? {
        val index = content.lastIndexOf('@')
        if (index == -1) return null
        return content.subSequence(0, index).toString()
    }

    fun checkValid(content: String): Boolean{
        val crc32 = CRC32()
        val index = content.lastIndexOf('@')
        if (index == -1) return false
        val payload = content.subSequence(0, index).toString().toByteArray()
        val expectedCRC = content.subSequence(index+1, content.length).toString().toLong()
        crc32.update(payload)
        return crc32.value == expectedCRC
    }

    fun generateCRC32(payload: String): String{
        val crc32 = CRC32()
        crc32.update(payload.toByteArray())
        return payload + "@" + crc32.value.toString()
    }

    fun testCRC32(){
        val strings = arrayOf("PizzaPasta", "MammaMia", "JohannesJoestar")
        for (str in strings){
            if (!checkValid(generateCRC32(str))) Log.e("Vault", "Nope")
        }
    }
}