package com.vaultionizer.vaultapp.cryptography

import com.vaultionizer.vaultapp.cryptography.crypto.CryptoMode
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoPadding
import com.vaultionizer.vaultapp.cryptography.crypto.CryptoType
import com.vaultionizer.vaultapp.cryptography.model.Password
import java.util.*

object CryptoUtils {
    /** This method generates and stores a cryptographic key inside the devices TPM.
     * @param spaceID : The ID of the space the key belongs to, cloud technically be a String but unnecessary in the context of this project.
     * @param cryptoType : The type of cryptographic algorithm that the key should be for (DES, AES, ...) - see available algorithms in crypto/CryptoType.
     * @param cryptoMode : The type of cryptographic block mode that the key should be for (CBC, GCM, ...) - see available modes in crypto/CryptoMode.
     * @param cryptoPadding : The type of cryptographic padding that the key should be for (NoPadding, ...) - see available padding in crypto/CryptoPadding.
     */
    fun generateKeyForSingleUserSpace(
        spaceID: Long,
        cryptoType: CryptoType,
        cryptoMode: CryptoMode,
        cryptoPadding: CryptoPadding
    ) {
        Cryptography.createSingleUserKey(spaceID, cryptoType, cryptoMode, cryptoPadding)
    }

    /** This method generates a cryptographic key outside the devices TPM. It encrypts the key with and the hashed password of the user. Stores the key in the local TPM and gives back the transferBytes.
     * @param spaceID : The ID of the space the key belongs to, cloud technically be a String but unnecessary in the context of this project.
     * @param cryptoType : The type of cryptographic algorithm that the key should be for (DES, AES, ...) - see available algorithms in crypto/CryptoType.
     * @param cryptoMode : The type of cryptographic block mode that the key should be for (CBC, GCM, ...) - see available modes in crypto/CryptoMode.
     * @param cryptoPadding : The type of cryptographic padding that the key should be for (NoPadding, ...) - see available padding in crypto/CryptoPadding.
     * @param password : The password the secret key is encrypted with.
     * @return Returns transferBytes which are a cipher of the symmetric key used for keeping a space safe.
     */
    fun generateKeyForSharedSpace(
        spaceID: Long,
        cryptoType: CryptoType,
        cryptoMode: CryptoMode,
        cryptoPadding: CryptoPadding,
        password: String
    ): ByteArray {
        return Cryptography.createSharedKey(spaceID, cryptoType, cryptoMode, cryptoPadding, Password(password.toByteArray(Charsets.UTF_8)))
    }

    /** This method is used to decrypt the received transferBytes with the transferPassword and if successfully, imports the key into the TPM of the local device.
     * @param spaceID : The ID of the space the key belongs to, cloud technically be a String but unnecessary in the context of this project.
     * @param transferBytes : The bytes of data exchanged between users for invitations into spaces.
     * @param password : The password the secret key was encrypted with.
     * @return Returns TRUE when import was fully successfully, FALSE if the import failed
     */
    fun importKeyForSharedSpace(spaceID: Long, transferBytes: ByteArray, password: String): Boolean {
        return Cryptography.importKey(spaceID, transferBytes, Password(password.toByteArray(Charsets.UTF_8)))
    }

    /** This method is used to delete keys from the Vaultionzer-Project out of the local TPM using an identifier. This method shall only be called if a space and all its data should be inaccessible or the users wants to change the key of the space having a full backup.
     * @param spaceID : The ID of the space the key belongs to, cloud technically be a String but unnecessary in the context of this project.
     * @return Returns TRUE, if the key was successfully deleted, FALSE when anything goes wrong
     */
    fun deleteKey(spaceID: Long): Boolean {
        return Cryptography.deleteKey(spaceID)
    }

    /** This method is used for listing all keys by their true alias.
     * @return Returns a list of String resembling all Vaultionizer keys in the TPM.
     */
    fun listKeys(): Enumeration<String> {
        return Cryptography.listKeys()
    }

    /** This method is used to check if a alias (here the spaceID) has a key in the local TPM.
     *  @param spaceID : The ID of the space the key belongs to, cloud technically be a String but unnecessary in the context of this project.
     *  @return Returns TRUE when a key with said alias is found otherwise returns FALSE.
     */
    fun existsKey(spaceID: Long): Boolean {
        return Cryptography.existsKey(spaceID)
    }

    /** This method is used to encrypt data with a key from the local TPM.
     *  @param spaceID : The ID of the space the key belongs to, cloud technically be a String but unnecessary in the context of this project.
     *  @param bytes : The data that is being encrypted.
     *  @return Returns the resulting cipher.
     */
    fun encryptData(spaceID: Long, bytes: ByteArray): ByteArray {
        return Cryptography.encryptor(spaceID, bytes)
    }

    /** This method is used to decrypt data with a key from the local TPM.
     *  @param spaceID : The ID of the space the key belongs to, cloud technically be a String but unnecessary in the context of this project.
     *  @param bytes : The data that is being decrypted (cipher).
     *  @return Returns the resulting plain text.
     */
    fun decryptData(spaceID: Long, bytes: ByteArray): ByteArray {
        return Cryptography.decrytor(spaceID, bytes)
    }
}