package com.vaultionizer.vaultapp.cryptography

import android.security.keystore.KeyInfo
import android.util.Log
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

const val KEY_PREFIX = "vaultionizer_"
const val PROVIDER = "AndroidKeyStore"

class Cryptography {

    fun getKey(spaceID : Long) : SecretKey{
        val keyStore : KeyStore = KeyStore.getInstance(PROVIDER)
        keyStore.load(null)

        val secretKeyEntry : KeyStore.SecretKeyEntry = keyStore.getEntry("$KEY_PREFIX$spaceID", null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    fun createKey(spaceID: Long, cryptoType: CryptoType, cryptoMode : CryptoMode, cryptoPadding : CryptoPadding) : SecretKey?{
        if (cryptoType == CryptoType.AES){
            if (cryptoMode == CryptoMode.GCM){
                if (cryptoPadding == CryptoPadding.NONE){
                    return AesGcmNopadding().generateKey("$KEY_PREFIX$spaceID")
                }
            }
            if (cryptoMode == CryptoMode.CBC){
                if (cryptoPadding == CryptoPadding.NONE){

                    return AesCbcNopadding().generateKey("$KEY_PREFIX$spaceID")
                }
            }
        }
        return null
    }

    fun deleteKey(spaceID : Long) : Boolean{
        val keyStore : KeyStore = KeyStore.getInstance(PROVIDER)
        try {
            keyStore.deleteEntry("$KEY_PREFIX$spaceID")
        } catch (e : KeyStoreException) {
            Log.e("CryptoError", "The key with $spaceID could not be deleted cause it was not found in $PROVIDER")
            return false
        }
        return true
    }

    fun padder(input : ByteArray) : ByteArray? {
        if (input.size % 16 == 0){
            return input
        }
        val due = 16 - (input.size % 16)
        return input + ByteArray(due) {0}
    }

    fun wrapper(pair : Pair<ByteArray,ByteArray>) : ByteArray?{
        return pair.first + pair.second
    }

    fun dewrapper(secretKey : SecretKey, warp : ByteArray) : Pair<ByteArray,ByteArray>? {
        return getCryptoClass(secretKey)?.dewrapper(warp)
    }

    fun encryptData(secretKey: SecretKey, message : ByteArray): Pair<ByteArray, ByteArray>? {
        return getCryptoClass(secretKey)?.encrypt(secretKey, message)
    }

    fun decryptData(secretKey: SecretKey, iv : ByteArray, message : ByteArray): ByteArray? {
        return getCryptoClass(secretKey)?.decrypt(secretKey, iv , message)

    }

    fun getCryptoClass(secretKey: SecretKey) : CryptoClass?{
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance(secretKey.algorithm, PROVIDER)
        val keyInfo: KeyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo

        keyInfo.blockModes
        if (secretKey.algorithm == CryptoType.AES.name){
            if (keyInfo.blockModes[0] == CryptoMode.GCM.name){
                if (keyInfo.encryptionPaddings[0] == CryptoPadding.NONE.name){
                    return AesGcmNopadding()
                }
            }
            if (keyInfo.blockModes[0] == CryptoMode.CBC.name){
                if (keyInfo.encryptionPaddings[0] == CryptoPadding.NONE.name){
                    return AesCbcNopadding()
                }
            }
        }
        return null
    }
}