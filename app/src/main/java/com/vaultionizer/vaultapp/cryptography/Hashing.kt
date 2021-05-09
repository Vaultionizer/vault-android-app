package com.vaultionizer.vaultapp.cryptography

import com.vaultionizer.vaultapp.cryptography.dataclasses.*
import java.security.MessageDigest
import org.mindrot.jbcrypt.BCrypt


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
    fun bCryptHash(pwd: Password) : HashSalt {
        val saltString = BCrypt.gensalt()
        val hashString = BCrypt.hashpw(pwd.toString(), saltString)

        val salt = Salt(saltString.toByteArray(Charsets.UTF_8))
        val hash = Hash(hashString.toByteArray(Charsets.UTF_8))

        return HashSalt(hash, salt)
    }

    /** Method to validate the password provided by the user against the data from the transfer
     * @param pwd : Password provided by the user
     * @param hashSalt : Object containing the hash and salt provided by transfer
     * @return boolean : returns TRUE when the password is valid, return FALSE when not
     */
    fun bCryptValidate(pwd : Password, hashSalt: HashSalt) : Boolean {
        val hashString = BCrypt.hashpw(pwd.toString(), hashSalt.salt.toString())
        val hash = Hash(hashString.toByteArray(Charsets.UTF_8))

        if (hash.hash.contentEquals(hashSalt.hash.hash)){
            return true
        }
        return false
    }
}