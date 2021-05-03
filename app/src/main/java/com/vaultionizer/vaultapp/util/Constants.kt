package com.vaultionizer.vaultapp.util

object Constants {

    /**
     * Application
     */
    const val WORK_MANAGER_SCHEDULER_LIMIT = 3
    const val DEFAULT_PROTOCOL = "https"

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
    const val VN_KEY_TRANSFER_SIZE = 32

    /**
     * WorkManager
     */
    const val WORKER_TAG_FILE = "FILE_WORKER"
    const val WORKER_FILE_PARENT_ID = "FILE_PARENT_ID"
    const val WORKER_FILE_LOCAL_ID = "FILE_LOCAL_ID"
    const val WORKER_FILE_URI = "FILE_URI"
    const val WORKER_SYNC_REQUEST_ID = "SYNC_REQUEST_ID"
    const val WORKER_SPACE_ID = "SPACE_ID"
    const val WORKER_FILE_BYTES = "FILE_BYTES"
}