package com.vaultionizer.vaultapp.cryptography

import com.vaultionizer.vaultapp.cryptography.model.Hash
import com.vaultionizer.vaultapp.cryptography.model.HashSalt
import com.vaultionizer.vaultapp.cryptography.model.Password
import com.vaultionizer.vaultapp.cryptography.model.Salt
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest


class Hashing {
    fun sha256(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    fun sha256(bytes: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes)
    }

    fun sha512(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    fun sha512(bytes: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-512")
        return md.digest(bytes)
    }

    /** Method to hash a password with a random generated salt
     * @param pwd : Password provided by the user
     * @return hashSalt : Hash of the password together with the used salt
     */
    fun bCryptHash(pwd: Password): HashSalt {
        val saltString = BCrypt.gensalt()
        val hashString = BCrypt.hashpw(pwd.toString(), saltString)

        val salt = Salt(saltString.toByteArray(Charsets.UTF_8))
        val hash = hashString.toByteArray(Charsets.UTF_8)

        val trueHash = Hash(hash.sliceArray(29 until hash.size) + "V".toByteArray(Charsets.UTF_8))

        return HashSalt(trueHash, salt)
    }

    /** Method to hash a password with a random generated salt
     * @param pwd : Password provided by the user
     * @return hashSalt : Hash of the password together with the used salt
     */
    fun bCryptHash(pwd: Password, salt: Salt): HashSalt {
        val saltString = salt.toString()
        val hashString = BCrypt.hashpw(pwd.toString(), saltString)

        val salt = Salt(saltString.toByteArray(Charsets.UTF_8))
        val hash = hashString.toByteArray(Charsets.UTF_8)

        val trueHash = Hash(hash.sliceArray(29 until hash.size) + "V".toByteArray(Charsets.UTF_8))

        return HashSalt(trueHash, salt)
    }

    /** Method to validate the password provided by the user against the data from the transfer
     * @param pwd : Password provided by the user
     * @param hashSalt : Object containing the hash and salt provided by transfer
     * @return boolean : returns TRUE when the password is valid, return FALSE when not
     */
    fun bCryptValidate(pwd: Password, hashSalt: HashSalt): Boolean {
        val hashString = BCrypt.hashpw(pwd.toString(), hashSalt.salt.toString())
        val hash = hashString.toByteArray(Charsets.UTF_8)

        val trueHash = Hash(hash.sliceArray(29 until hash.size) + "V".toByteArray(Charsets.UTF_8))

        if (trueHash.hash.contentEquals(hashSalt.hash.hash)) {
            return true
        }
        return false
    }
}