package com.vaultionizer.vaultapp.cryptography

import android.security.keystore.KeyInfo
import android.util.Log
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import com.vaultionizer.vaultapp.cryptography.model.*
import com.vaultionizer.vaultapp.util.Constants
import java.security.KeyStore
import java.security.KeyStoreException
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.SecretKeySpec


object Cryptography {

    // TODO: Import- / Export-Keys may have different ciphers, block-modes or paddings. ATM only AesGcmNopadding. This also applies for the transferred keys themself

    private val checkMarker = ByteArray(16)

    fun getKey(spaceID: Long): SecretKey {
        val keyStore: KeyStore = KeyStore.getInstance(Constants.VN_KEYSTORE_PROVIDER)
        keyStore.load(null)

        val secretKeyEntry: KeyStore.SecretKeyEntry =
            keyStore.getEntry("${Constants.VN_KEY_PREFIX}$spaceID", null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    fun createSingleUserKey(
        spaceID: Long,
        cryptoType: CryptoType,
        cryptoMode: CryptoMode,
        cryptoPadding: CryptoPadding
    ) {
        if (!existsKey(spaceID)) {
            if (cryptoType == CryptoType.AES) {
                singeKeyAlgorithmAES(spaceID, cryptoMode, cryptoPadding)
                return
            }
            throw RuntimeException("Unsupported Key Type")
        }
        throw RuntimeException("Key already exists")
    }

    fun createSharedKey(
        spaceID: Long,
        cryptoType: CryptoType,
        cryptoMode: CryptoMode,
        cryptoPadding: CryptoPadding,
        password: Password
    ): ByteArray {
        if (cryptoType == CryptoType.AES) {
            if (cryptoMode == CryptoMode.GCM) {
                if (cryptoPadding == CryptoPadding.NoPadding) {

                    val sharedKeyOutput = AesGcmNopadding.generateSharedKey(
                        "${Constants.VN_KEY_PREFIX}$spaceID",
                        password
                    )
                    return wrapper(
                        sharedKeyOutput.salt,
                        wrapper(sharedKeyOutput.iv, sharedKeyOutput.cipher)
                    )

                }
            }
            if (cryptoMode == CryptoMode.CBC) {
                if (cryptoPadding == CryptoPadding.NoPadding) {
                    throw RuntimeException("Unsupported Key Type")
//                    TODO look top
//                    val sharedKeyOutput = AesCbcNopadding().generateSharedKey(
//                        "${Constants.VN_KEY_PREFIX}$spaceID",
//                        password
//                    )
//                    return wrapper(
//                        sharedKeyOutput.salt,
//                        wrapper(sharedKeyOutput.iv, sharedKeyOutput.cipher)
//                    )

                }
            }
        }
        throw RuntimeException("Unsupported Key Type")
    }

    private fun singeKeyAlgorithmAES(
        spaceID: Long,
        cryptoMode: CryptoMode,
        cryptoPadding: CryptoPadding
    ) {
        if (cryptoMode == CryptoMode.GCM) {
            if (cryptoPadding == CryptoPadding.NoPadding) {

                AesGcmNopadding.generateSingleUserKey("${Constants.VN_KEY_PREFIX}$spaceID")
                return
            }
        }
        if (cryptoMode == CryptoMode.CBC) {
            if (cryptoPadding == CryptoPadding.NoPadding) {

                AesCbcNopadding.generateSingleUserKey("${Constants.VN_KEY_PREFIX}$spaceID")
                return
            }
        }
    }

    fun importKey(spaceID: Long, bytes: ByteArray, pwd: Password): Boolean {
        val saltIvcipher = desalter(bytes)
        val salt = Salt(saltIvcipher.salt)
        val importKey =
            SecretKeySpec(Hashing.bCryptHash(pwd, salt).hash.toByteArray(), "AES")
        val keyPlainUnchecked = AesGcmNopadding.decrypt(            // TODO generalize when more exchange key types are available
            importKey,
            saltIvcipher.ivcipher.iv,
            saltIvcipher.ivcipher.cipher
        )
        if (validate(keyPlainUnchecked)) {
            val keyPlain = keyPlainUnchecked.sliceArray(16 until keyPlainUnchecked.size)
            val secretKey = SecretKeySpec(keyPlain, "AES")
            AesGcmNopadding.addKeyToKeyStore(secretKey, "${Constants.VN_KEY_PREFIX}$spaceID") // TODO generalize when more exchange key types are available

            return true
        }
        return false

    }

    fun encryptor(spaceID: Long, bytes: ByteArray): ByteArray {
        if (existsKey(spaceID)){
            return wrapper(encryptData(getKey(spaceID), bytes))
        }
        throw RuntimeException("Tried to encrypted with a nonexistent key")
    }

    fun decrytor(spaceID: Long, bytes: ByteArray): ByteArray {
        val secretKey = getKey(spaceID)
        val pairIvCipher = dewrapper(secretKey, bytes)
        val iv = pairIvCipher.iv
        val cipher = pairIvCipher.cipher

        return decryptData(secretKey, iv, cipher)
    }

    fun deleteKey(spaceID: Long): Boolean {
        val keyStore: KeyStore = KeyStore.getInstance(Constants.VN_KEYSTORE_PROVIDER)
        keyStore.load(null)

        if (existsKey(spaceID)) {
            try {
                keyStore.deleteEntry("${Constants.VN_KEY_PREFIX}$spaceID")
            } catch (e: KeyStoreException) {
                Log.e(
                    Constants.VN_TAG,
                    "The key with $spaceID could not be deleted cause it was not found in ${Constants.VN_KEYSTORE_PROVIDER}",
                    e
                )
                return false
            }
            return true
        }
        return false
    }


    fun generateImportExportKeyAndSalt(pwd: Password): KeySalt {
        val hashSalt = Hashing.bCryptHash(pwd)
        return KeySalt(SecretKeySpec(hashSalt.hash.hash, "AES"), hashSalt.salt.salt)
    }

    fun validate(keyPlainUnchecked: ByteArray): Boolean {
        val checkMark = keyPlainUnchecked.sliceArray(0 until 16)
        checkMark.forEach {
            if (it != 0.toByte()) {
                throw RuntimeException("Unsuccessful Transfer: Check mark failed")
            }
        }
        return true
    }

    fun wrapper(pair: IvCipher): ByteArray {
        return pair.iv + pair.cipher
    }

    fun wrapper(byteArray1: ByteArray, byteArray2: ByteArray): ByteArray {
        return byteArray1 + byteArray2
    }

    fun dewrapper(secretKey: SecretKey, warp: ByteArray): IvCipher {
        val ivLength = getCryptoClass(secretKey).BLOCK_MODE_IV_SIZE
        val iv: ByteArray = warp.sliceArray(0 until ivLength)
        val cipherText: ByteArray = warp.sliceArray(ivLength until warp.size)

        return IvCipher(iv, cipherText)
    }

    fun dewrapper(warp: ByteArray, ivLength : Int): IvCipher{
        val iv: ByteArray = warp.sliceArray(0 until ivLength)
        val cipherText: ByteArray = warp.sliceArray(ivLength until warp.size)

        return IvCipher(iv, cipherText)
    }

    fun desalter(bytes: ByteArray): SaltIvcipher {
        val salt = bytes.sliceArray(0 until 29)
        val bytesivCipher = bytes.sliceArray(29 until bytes.size)
        val ivCipher = dewrapper(bytesivCipher, AesGcmNopadding.BLOCK_MODE_IV_SIZE)             // TODO generalize when more exchange key types are available

        return SaltIvcipher(salt, ivCipher)
    }

    fun encryptData(secretKey: SecretKey, message: ByteArray): IvCipher {
        return getCryptoClass(secretKey).encrypt(secretKey, message)
    }

    fun decryptData(secretKey: SecretKey, iv: ByteArray, message: ByteArray): ByteArray {
        return getCryptoClass(secretKey).decrypt(secretKey, iv, message)
    }

    private fun getCryptoClass(secretKey: SecretKey): CryptoClass {
        val factory: SecretKeyFactory =
            SecretKeyFactory.getInstance(secretKey.algorithm, Constants.VN_KEYSTORE_PROVIDER)
        val keyInfo: KeyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo

        keyInfo.blockModes
        if (secretKey.algorithm == CryptoType.AES.name) {
            if (keyInfo.blockModes[0] == CryptoMode.GCM.name) {
                if (keyInfo.encryptionPaddings[0] == CryptoPadding.NoPadding.name) {
                    return AesGcmNopadding
                }
            }
            if (keyInfo.blockModes[0] == CryptoMode.CBC.name) {
                if (keyInfo.encryptionPaddings[0] == CryptoPadding.NoPadding.name) {
                    return AesCbcNopadding
                }
            }
        }
        throw RuntimeException("Unsupported Key Type")
    }

    fun listKeys(): Enumeration<String> {
        val keyStore: KeyStore = KeyStore.getInstance(Constants.VN_KEYSTORE_PROVIDER)
        keyStore.load(null)

        return keyStore.aliases()
    }

    fun existsKey(spaceID: Long): Boolean {
        val keyStore: KeyStore = KeyStore.getInstance(Constants.VN_KEYSTORE_PROVIDER)
        keyStore.load(null)

        return keyStore.containsAlias("${Constants.VN_KEY_PREFIX}$spaceID")
    }
}