package com.vaultionizer.vaultapp.cryptography

import android.security.keystore.KeyInfo
import android.util.Log
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import com.vaultionizer.vaultapp.cryptography.dataclasses.IvCipher
import com.vaultionizer.vaultapp.cryptography.dataclasses.KeySalt
import com.vaultionizer.vaultapp.cryptography.dataclasses.SaltIvcipher
import com.vaultionizer.vaultapp.util.Constants
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.SecretKeySpec


class Cryptography {

    // TODO: Import- / Export-Keys may have different ciphers, block-modes or paddings. ATM only AesGcmNopadding. This also applies for the transferred keys themself

    // TODO: Check if a password was wrong and abort

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
        if (cryptoType == CryptoType.AES) {
            if (cryptoMode == CryptoMode.GCM) {
                if (cryptoPadding == CryptoPadding.NONE) {

                    AesGcmNopadding().generateSingleUserKey("${Constants.VN_KEY_PREFIX}$spaceID")

                }
            }
            if (cryptoMode == CryptoMode.CBC) {
                if (cryptoPadding == CryptoPadding.NONE) {

                    AesCbcNopadding().generateSingleUserKey("${Constants.VN_KEY_PREFIX}$spaceID")
                }
            }
        }
        throw RuntimeException("Unsupported Key Type")
    }

    fun createSharedKey(
        spaceID: Long,
        cryptoType: CryptoType,
        cryptoMode: CryptoMode,
        cryptoPadding: CryptoPadding,
        password: String
    ): ByteArray {
        if (cryptoType == CryptoType.AES) {
            if (cryptoMode == CryptoMode.GCM) {
                if (cryptoPadding == CryptoPadding.NONE) {

                    val sharedKeyOutput = AesGcmNopadding().generateSharedKey(
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
                if (cryptoPadding == CryptoPadding.NONE) {
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

    fun importKey(spaceID: Long, bytes: ByteArray, password: String): Boolean {
        val saltIvcipher = desalter(bytes)
        val importKey =
            SecretKeySpec(Hashing().sha256(password.toByteArray() + saltIvcipher.salt), "AES")
        val keyPlain = AesGcmNopadding().decrypt(
            importKey,
            saltIvcipher.ivcipher.iv,
            saltIvcipher.ivcipher.cipher
        )
        val secretKey = SecretKeySpec(keyPlain, "AES")

        AesGcmNopadding().addKeyToKeyStore(secretKey, "${Constants.VN_KEY_PREFIX}$spaceID")

        return true
    }

    fun encryptor(spaceID: Long, bytes: ByteArray): ByteArray {
        return wrapper(encryptData(getKey(spaceID), padder(bytes)))
    }

    fun encryptorNoPadder(spaceID: Long, bytes: ByteArray): ByteArray {
        return wrapper(encryptData(getKey(spaceID), bytes))
    }

    fun decrytor(spaceID: Long, bytes: ByteArray): ByteArray {
        val secretKey = getKey(spaceID)
        val pairIvCipher = dewrapper(secretKey, bytes)
        val iv = pairIvCipher.iv
        val cipher = pairIvCipher.cipher

        return dewrapper(secretKey, decryptData(secretKey, iv, cipher)).cipher
    }

    fun deleteKey(spaceID: Long): Boolean {
        val keyStore: KeyStore = KeyStore.getInstance(Constants.VN_KEYSTORE_PROVIDER)
        keyStore.load(null)
        try {
            Log.e("Vault", "${Constants.VN_KEY_PREFIX}$spaceID")
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

    fun generateImportExportKeyAndSalt(password: String): KeySalt {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return KeySalt(SecretKeySpec(Hashing().sha256(password.toByteArray() + salt), "AES"), salt)
    }

    fun padder(input: ByteArray): ByteArray {
        if (input.size % 16 == 0) {
            return input
        }
        val due = 16 - (input.size % 16)
        return input + ByteArray(due) { 0 }
    }

    fun wrapper(pair: IvCipher): ByteArray {
        return pair.iv + pair.cipher
    }

    fun wrapper(byteArray1: ByteArray, byteArray2: ByteArray): ByteArray  {
        return byteArray1 + byteArray2
    }

    fun dewrapper(secretKey: SecretKey, warp: ByteArray): IvCipher {
        return getCryptoClass(secretKey).dewrapper(warp)
    }

    fun desalter(bytes: ByteArray): SaltIvcipher {
        val salt = bytes.sliceArray(0 until Constants.VN_KEY_TRANSFER_SIZE)
        val bytesivCipher = bytes.sliceArray(Constants.VN_KEY_TRANSFER_SIZE until bytes.size)
        val ivCipher = AesGcmNopadding().dewrapper(bytesivCipher)

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
                if (keyInfo.encryptionPaddings[0] == CryptoPadding.NONE.name) {
                    return AesGcmNopadding()
                }
            }
            if (keyInfo.blockModes[0] == CryptoMode.CBC.name) {
                if (keyInfo.encryptionPaddings[0] == CryptoPadding.NONE.name) {
                    return AesCbcNopadding()
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

    fun isKeyAvailable(spaceID: Long): Boolean {
        val keyStore: KeyStore = KeyStore.getInstance(Constants.VN_KEYSTORE_PROVIDER)
        keyStore.load(null)

        return keyStore.containsAlias("${Constants.VN_KEY_PREFIX}$spaceID")
    }
}