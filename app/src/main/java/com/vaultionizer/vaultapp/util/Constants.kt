package com.vaultionizer.vaultapp.util

object Constants {

    /**
     * Application
     */
    const val WORK_MANAGER_SCHEDULER_LIMIT = 3

    /**
     * Meta
     */
    const val MIN_SERVER_VERSION = "0.1"
    const val SESSION_TOKEN_LIFETIME = 30 * 60 * 1000

    /**
     * Internal File System
     */
    const val VN_FILE_SUFFIX = "evn"

    /**
     * Logging
     */
    const val VN_TAG = "Vault"

    /**
     * Crypto
     */
    const val VN_KEY_PREFIX = "vaultionizer_"
    const val VN_KEYSTORE_PROVIDER = "AndroidKeyStore"
    const val VN_KEY_TRANSFER_SIZE = 256
}