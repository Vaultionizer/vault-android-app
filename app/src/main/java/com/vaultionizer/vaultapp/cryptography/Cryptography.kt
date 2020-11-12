package com.vaultionizer.vaultapp.cryptography

import android.security.keystore.KeyInfo
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

const val KEY_PREFIX = "vaultionizer_"
const val PROVIDER = "AndroidKeyStore"

class Cryptography {

    fun getKeyBySpaceID(spaceID : Long) : SecretKey{
        val keyStore : KeyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val secretKeyEntry : KeyStore.SecretKeyEntry = keyStore.getEntry("$KEY_PREFIX$spaceID", null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    fun createKey(keystoreAlias : String, cryptoType: CryptoType, cryptoMode : CryptoMode, cryptoPadding : CryptoPadding) : SecretKey?{
        if (cryptoType == CryptoType.AES){
            if (cryptoMode == CryptoMode.GCM){
                if (cryptoPadding == CryptoPadding.NONE){
                    return AesGcmNopadding().generateKey(keystoreAlias)
                }
            }
            if (cryptoMode == CryptoMode.CBC){
                if (cryptoPadding == CryptoPadding.NONE){
                    return AesCbcNopadding().generateKey(keystoreAlias)
                }
            }
        }
        return null
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
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance(secretKey.algorithm, "AndroidKeyStore")
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